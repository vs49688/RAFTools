/*
 * RAFTools - Copyright (C) 2015 Zane van Iperen.
 *    Contact: zane.vaniperen@uqconnect.edu.au
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2, and only
 * version 2 as published by the Free Software Foundation. 
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Any and all GPL restrictions may be circumvented with permission from the
 * the original author.
 */
package net.vs49688.rafview.vfs;

import net.vs49688.rafview.sources.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.Paths;
import net.vs49688.rafview.vfs.FileNode.*;

public class RAFS {
	/** The magic number of a .raf file */
	private static final int RAFIDX_MAGIC = 0x18BE0EF0;
	
	/** The root of the VFS */
	private final DirNode m_Root;
	
	/** The mapping of paths to their RAFDataFile objects */
	private final Map<Path, PossiblyZippedSynchronisedFile> m_DataFiles;

	private final IOperationsNotify m_NotifyDispatch;
	private final List<IOperationsNotify> m_Notify;
	
	/**
	 * Constructs a new, empty VFS
	 */
	public RAFS() {
		m_NotifyDispatch = new _NotifyDispatch();
		m_Notify = new ArrayList<>();
		m_Root = new RootNode(m_NotifyDispatch);
		
		m_DataFiles = new HashMap<>();
	}
	
	/**
	 * Add a new notify handler.
	 * This causes the the onAdd() function in the handler to be
	 * called for every node in the tree.
	 * @param ion The notify handler.
	 */
	public void addNotifyHandler(IOperationsNotify ion) {
		if(ion == null || m_Notify.contains(ion))
			return;
		
		/* We're a new handler, so we don't know the existing
		 * tree structure. Let's rebuild it for them! */
		_reprocess(m_Root, ion);
		m_Notify.add(ion);
	}

	/**
	 * Simulates the rebuilding the tree.
	 * @param n The root node.
	 * @param notify The notify handler.
	 */
	private void _reprocess(Node n, IOperationsNotify notify) {
		notify.onAdd(n);
		
		if(n instanceof DirNode) {
			for(final Node c : (DirNode)n)
				_reprocess(c, notify);
		}
	}
	
	/**
	 * Add a RAF Archive to the VFS.
	 * @param raf The path to the index (.raf)
	 * @param dat The path to the data (.raf.dat)
	 * @param versionName The version of this file.
	 * @throws IOException If an I/O error occurred.
	 */
	public void addFile(Path raf, Path dat, String versionName) throws IOException {
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
				throw new IOException(String.format("%s: Invalid magic number. Expected 0x%X, got 0x%X\n", versionName, RAFIDX_MAGIC, magic));

			/* Make sure we're version 1 */
			if((version = buffer.getInt()) != 1)
				throw new IOException(String.format("%s: Unsupported version %d\n", versionName, version));

			/* No idea what this does. Appears to be always 0 */
			mgrIndex = buffer.getInt();
			
			if(mgrIndex != 0) {
				System.err.printf("%s: WARNING: mgrIndex field non-zero. Please take note of this and email the developer.\n", versionName);
			}
			
			/* Read the file list and string offsets */
			lOffset = buffer.getInt();
			sOffset = buffer.getInt();
			
			/* Read the string table */
			buffer.position(sOffset);
			List<String> st = readStringTable(buffer);

			/* A mapping of the offsets in the string table to their FileNode */
			List<FileNode> indexMap = new ArrayList<>(st.size());
			
			for(final String s : st) {
				Path path = Paths.get(s);
				
				DirNode node = m_Root;
				
				/* Traverse the directory tree, creating directories if required */
				for(int i = 0; i < path.getNameCount()-1; ++i) {
					node = node.getAddDirectory(path.getName(i).toString());
				}

				FileNode newNode = null;
				for(final Node n : node) {
					if(n.name().equalsIgnoreCase(path.getFileName().toString())) {
						if(!(n instanceof FileNode)) {
							throw new IOException(String.format("%s: Overwriting directory with file (%s)", raf, n.getFullPath()));
						}
						newNode = (FileNode)n;
						break;
					}
				}
				
				if(newNode == null) {
					newNode = (FileNode)node.addChild(new FileNode(path.getName(path.getNameCount()-1).toString(), m_NotifyDispatch));
				}
				
				indexMap.add(newNode);
			}

			/* Read the file list */
			buffer.position(lOffset);
			readFileList(buffer, indexMap, st, versionName, dat);
		}
	}

	public Node getRoot() {
		return m_Root;
	}
	

	private static int getPathHash(String s) {
		int hash = 0, tmp = 0;
		
		s = s.toLowerCase();
		for(int i = 0; i < s.length(); ++i) {
			hash = (hash << 4) + s.charAt(i);
			tmp = hash & 0xF0000000;
			if(tmp != 0) {
				hash = hash ^ (tmp >>> 24);
				hash = hash ^ tmp;
			}
		}
		
		return hash;
	}

	/**
	 * Read the file table, adding data sources to all the FileNodes.
	 * @param b The ByteBuffer containing the data. Is expected to be at the 
	 * position where the file table starts.
	 * @param indexMap The mapping of string table indices to their FileNodes.
	 * @param rawPaths The list of raw paths (using the same indices as indexMap).
	 * @param version The version of files in the file table. Should be of the
	 * form "X.X.X.X"
	 * @param dat The path to the data file.
	 * @throws IOException If an I/O error occurred.
	 */
	private void readFileList(ByteBuffer b, List<FileNode> indexMap, List<String> rawPaths, String version, Path dat) throws IOException {
		int numFiles = b.getInt();
		
		// FIXME: assert(numFiles == stringtable.size())
		
		for(int i = 0; i < numFiles; ++i) {
			int hash = b.getInt();
			
			int offset = b.getInt();
			int size = b.getInt();
			int index = b.getInt();
			
			int calcHash = getPathHash(rawPaths.get(index));
			
			if(hash != calcHash) {
				throw new IOException(String.format("%s: Hash mismatch for %s. Expected %d, got %d", version, rawPaths.get(index), calcHash, hash));
			}
			
			FileNode fn = indexMap.get(index);
			
			/* If we've already got this file loaded, don't load it again */
			PossiblyZippedSynchronisedFile rdf;
			if(m_DataFiles.containsKey(dat)) {
				rdf = m_DataFiles.get(dat);
			} else {
				rdf = new PossiblyZippedSynchronisedFile(dat);
				m_DataFiles.put(dat, rdf);
			}
			
			fn.addVersion(version, rdf.createDataSource(offset, size));
		}
	}
	
	public void clear() {
		m_NotifyDispatch.onClear();
		m_Root.delete();
	}
	
	/**
	 * 
	 */
	public void dumpPaths(PrintStream stream) {
		_dumpPaths(m_Root, stream, 0);
	}
	
	private void _dumpPaths(Node node, PrintStream stream, int tab) {
		
		for(int i = 0; i < tab; ++i)
			stream.print("  ");

		stream.printf("%d: %s%s\n", node.getUID(),
				node.name() == null ? "" : node.name(),
				node instanceof DirNode ? "/" : "");
		
		if(node instanceof FileNode) {
			FileNode fn = (FileNode)node;
			
			for(final FileNode.Version v : fn.getVersions()) {
				for(int i = 0; i <= tab; ++i)
					stream.print("  ");
				
				stream.printf("V: %s\n", v.toString());
			}
		}
		
		if(node instanceof DirNode) {
			for(final Node n : (DirNode)node)
				_dumpPaths(n, stream, tab + 1);	
		}
	}

	/**
	 * Extract a file or directory from the VFS.
	 * @param vfsPath The path to extract.
	 * @param outPath The output file/directory.
	 * @param version The version of the file to extract. If vfsPath is a directory, then
	 * this parameter is ignored.
	 * @throws IOException If an I/O error occurs.
	 */
	public void extract(Path vfsPath, Path outPath, String version) throws IOException {
		
		if(vfsPath == null)
			throw new IllegalArgumentException("vfsPath cannot be null");
		
		if(outPath == null)
			throw new IllegalArgumentException("outPath cannot be null");
		
		Node node = _findNodeByName(m_Root, vfsPath, 0);
		
		if(node == null)
			throw new IOException("Not found");
		
		if(node instanceof DirNode) {
			version = null;
		}

		_extractNode(node, outPath, version);

		m_NotifyDispatch.onComplete();
	}

	private void _extractNode(Node root, Path outDir, String version) throws IOException {
		if(root instanceof FileNode) {
			FileNode fn = (FileNode)root;
			FileNode.Version ver = null;
			
			if(version == null || version.isEmpty()) {
				ver = fn.getLatestVersion();
			} else {
				for(final Version v : fn.getVersions()) {
					if(v.toString().equalsIgnoreCase(version)) {
						ver = v;
						break;
					}
				}
			}
			
			if(ver != null) {
				Files.write(outDir, ver.getSource().read());
			}
			return;
		}
		
		DirNode dn = (DirNode)root;
		outDir = outDir.resolve(root.name());
		Files.createDirectories(outDir);
		
		for(final Node n: dn) {
			if(n instanceof DirNode)
				_extractNode(n, outDir, version);
			else
				_extractNode(n, outDir.resolve(n.name()), version);
		}
	}
	
	/**
	 * 
	 * @param vfsPath
	 * @return 
	 */
	public Node getNodeFromPath(Path vfsPath) {
		return _findNodeByName(m_Root, vfsPath.normalize(), 0);
	}
	
	public Node getNodeFromPath(Node root, Path vfsPath) {
		if(root instanceof FileNode) {
			throw new IllegalArgumentException("root is not a directory");
		}
		
		return _findNodeByName(root, vfsPath.normalize(), 0);
	}
	
	/**
	 * Traverse the tree using a string as a path
	 * @param root The node to start searching from. Must be a directory.
	 * @param path The path we're searching for.
	 * @param component The component index we're up to in the path.
	 * @return If found, the node. Otherwise, null.
	 */
	private Node _findNodeByName(Node root, Path path, int component) {
		
		if(component == path.getNameCount())
			return root;

		DirNode n = (DirNode)root;
		
		Path pcomp = path.getName(component);
		String compName = pcomp.toString();
		
		if(compName.equals(".") || compName.isEmpty()) {
			return _findNodeByName(root, path, component+1);
		} else if(compName.equals("..")) {
			DirNode parent = root.getParent();
			/* If we have no parent, we're at the root, so stay there */
			if(parent == null)
				parent = n;
			
			return _findNodeByName(parent, path, component+1);
		}
		
		for(final Node c : n) {
			if(c.name().equalsIgnoreCase(compName)) {
				return _findNodeByName(c, path, component + 1);
			}
		}
		
		return null;
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
		Path path = Paths.get(dir.toString(), node.name() == null ? "" : node.name());
		
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
			
			byte[] data = fn.getLatestVersion().getSource().read();
			
			System.out.printf("Writing %s\n", path);
			Files.createFile(path);
			Files.write(path, data);
		}
	}
	
	/**
	 * Fire a completion event.
	 */
	public void fireCompletion() {
		m_NotifyDispatch.onComplete();
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
	
	private class _NotifyDispatch implements IOperationsNotify {

		@Override
		public void onClear() {
			m_Notify.stream().forEach((ion) -> {
				ion.onClear();
			});
		}

		@Override
		public void onModify(Node n) {
			m_Notify.stream().forEach((ion) -> {
				ion.onModify(n);
			});
		}
		
		@Override
		public void onAdd(Node n) {
			m_Notify.stream().forEach((ion) -> {
				ion.onAdd(n);
			});
		}
		
		@Override
		public void onExtract(Node n) {
			m_Notify.stream().forEach((ion) -> {
				ion.onExtract(n);
			});
		}
		
		@Override
		public void onComplete() {
			m_Notify.stream().forEach((ion) -> {
				ion.onComplete();
			});
		}
	}
}
