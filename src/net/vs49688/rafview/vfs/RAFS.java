package net.vs49688.rafview.vfs;

import net.vs49688.rafview.sources.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.Paths;

public class RAFS {
	/** The magic number of a .raf file */
	private static final int RAFIDX_MAGIC = 0x18BE0EF0;
	
	/** The root of the VFS */
	private final DirNode m_Root;
	
	/** The mapping of paths to their RAFDataFile objects */
	private final Map<Path, RAFDataFile> m_DataFiles;

	/**
	 * Constructs a new, empty VFS
	 */
	public RAFS() {
		m_Root = new DirNode();
		m_DataFiles = new HashMap<>();
	}
	
	/**
	 * Add a RAF Archive to the VFS.
	 * @param raf The path to the index (.raf)
	 * @param dat The path to the data (.raf.dat)
	 * @throws IOException If an I/O error occurred.
	 */
	public void addFile(Path raf, Path dat) throws IOException {
		int lOffset, sOffset;
		int magic, version, mgrIndex;

		try(FileInputStream rfis = new FileInputStream(raf.toFile())) {
			MappedByteBuffer buffer;
			
			/* Map the file into memory */
			try (FileChannel fChannel = rfis.getChannel()) {
				buffer = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size());
				buffer.order(ByteOrder.LITTLE_ENDIAN);
			}

			/* Check the magic number */
			if((magic = buffer.getInt()) != RAFIDX_MAGIC)
				throw new IOException(String.format("Invalid magic number. Expected 0x%X, got 0x%X\n", RAFIDX_MAGIC, magic));

			/* Make sure we're version 1 */
			if((version = buffer.getInt()) != 1)
				throw new IOException(String.format("Unsupported version %d\n", version));

			/* No idea what this does */
			mgrIndex = buffer.getInt();
			
			/* Read the file list and string offsets */
			lOffset = buffer.getInt();
			sOffset = buffer.getInt();
			
			/* Read the string table */
			buffer.position(sOffset);
			List<String> st = readStringTable(buffer);

			/* A mapping of the offsets in the string table to their FileNode */
			List<FileNode> indexMap = new ArrayList<>(st.size());
			
			st.stream().map((s) -> Paths.get(s)).forEach((path) -> {
				DirNode node = m_Root;
				
				/* Traverse the directory tree, creating directories if required */
				for(int i = 0; i < path.getNameCount()-1; ++i)
					node = node.getAddDirectory(path.getName(i).toString());

				/* Add the file */
				indexMap.add((FileNode)node.AddChild(new FileNode(path.getName(path.getNameCount()-1).toString())));
			});

			/* Read the file list */
			buffer.position(lOffset);
			readFileList(buffer, indexMap, dat);
		}
	}

	public Node getRoot() {
		return m_Root;
	}
	
	/**
	 * Read the file table, adding data sources to all the FileNodes.
	 * @param b The ByteBuffer containing the data. Is expected to be at the 
	 * position where the file table starts.
	 * @param indexMap The mapping of string table indices to their FileNodes.
	 * @param dat The path to the data file.
	 * @throws IOException If an I/O error occurred.
	 */
	private void readFileList(ByteBuffer b, List<FileNode> indexMap, Path dat) throws IOException {
		int numFiles = b.getInt();
		
		// FIXME: assert(numFiles == stringtable.size())
		
		for(int i = 0; i < numFiles; ++i) {
			int unk = b.getInt(); // Not sure what this is
			
			int offset = b.getInt();
			int size = b.getInt();
			int index = b.getInt();
			
			FileNode fn = indexMap.get(index);

			/* If we've already got this file loaded, don't load it again */
			RAFDataFile rdf;
			if(m_DataFiles.containsKey(dat)) {
				rdf = m_DataFiles.get(dat);
			} else {
				rdf = new RAFDataFile(dat);
				m_DataFiles.put(dat, rdf);
			}
			
			fn.setSource(rdf.createDataSource(offset, size));
		}
	}
	

	/**
	 * 
	 */
	public void dumpPaths() {
		_dumpPaths(m_Root, 0);
	}
	
	private void _dumpPaths(Node node, int tab) {
		
		for(int i = 0; i < tab; ++i)
			System.out.print("  ");

		System.out.printf("%d: %s%s\n", node.getUID(),
				node.getName() == null ? "" : node.getName(),
				node instanceof DirNode ? "/" : "");
		
		if(node instanceof DirNode) {
			for(final Node n : (DirNode)node)
				_dumpPaths(n, tab + 1);	
		}
	}

	/**
	 * Dump the entire RAF FileSystem to a folder.
	 * @param dir The base directory to write everything to.
	 * @throws IOException If an I/O error occurred.
	 */
	public void dumpToDir(String dir) throws IOException {
		
		System.out.printf("Creating %s\n", dir);
		try {
			Files.createDirectory(Paths.get(dir));
		} catch(FileAlreadyExistsException e) {}
		
		_dumpToDir(Paths.get(dir), m_Root);
	}

	private void _dumpToDir(Path dir, Node node) throws IOException {
		Path path = Paths.get(dir.toString(), node.getName() == null ? "" : node.getName());
		
		/* Handle DirNodes */
		if(node instanceof DirNode) {
			DirNode dn = (DirNode)node;
			
			System.out.printf("Creating %s\n", path);
			
			try {
				Files.createDirectory(path);
			} catch(FileAlreadyExistsException e) {}
			
			for(final Node n : dn)
				_dumpToDir(path, n);

		/* Handle FileNodes */
		} else if(node instanceof FileNode) {
			FileNode fn = (FileNode)node;
			
			byte[] data = fn.getSource().read();
			
			System.out.printf("Writing %s\n", path);
			Files.createFile(path);
			Files.write(path, data);
		}
	}
	
	/**
	 * Read the string table.
	 * @param b The ByteBuffer containing the data. Is expected to be at the 
	 * position where the string table starts.
	 * @return A list of the strings contained within the string table.
	 */
	private static List<String> readStringTable(ByteBuffer b) {
		class Index {
			int offset;	// Offset from table index
			int size;	// Length of the string
		}
		
		int pos = b.position();
		int dataSize = b.getInt();
		int numStrings = b.getInt();

		List<String> list = new ArrayList<>(numStrings);
		
		Index[] idx = new Index[numStrings];
		
		/* Read the offsets and length */
		for(int i = 0; i < numStrings; ++i) {
			idx[i] = new Index();
			idx[i].offset = b.getInt();
			idx[i].size = b.getInt();
		}
		
		/* Now read the actual strings */
		for(int i = 0; i < numStrings; ++i) {
			b.position(pos + idx[i].offset);
			
			byte[] tmp = new byte[idx[i].size-1];
			b.get(tmp, 0, idx[i].size-1);

			/* Are they US-ASCII? They seem to be for now */
			list.add(new String(tmp, Charset.forName("US-ASCII")));
		}

		return list;
	}
}
