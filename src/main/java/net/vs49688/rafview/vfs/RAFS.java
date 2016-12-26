/*
 * RAFTools - Copyright (C) 2016 Zane van Iperen.
 *    Contact: zane@zanevaniperen.com
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
import com.google.common.jimfs.*;
import com.google.common.jimfs.PathType;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import net.vs49688.rafview.IPv4Sorter;
import net.vs49688.rafview.cli.Model;

public class RAFS {

	/**
	 * The magic number of a .raf file
	 */
	private static final int RAFIDX_MAGIC = 0x18BE0EF0;

	/**
	 * The mapping of paths to their RAFDataFile objects
	 */
	private final Map<Path, PossiblyZippedSynchronisedFile> m_DataFiles;

	private final IOperationsNotify m_NotifyDispatch;
	private final List<IOperationsNotify> m_Notify;

	private final FileSystem m_FileSystem;

	private final UserPrincipal m_RAFOwnerPrincipal;

	/**
	 * The mapping of paths to their versions.
	 */
	private final Map<Path, TreeSet<Version>> m_VersionData;

	public static void main(String[] args) throws Exception {
		Model model = new Model();
		model.openLolDirectory(Paths.get("E:\\Games\\League of Legends"));

		model.getVFS().writeToArchive(Paths.get("E:\\test.raf"));
	}

	/**
	 * Constructs a new, empty VFS
	 */
	public RAFS() {
		m_NotifyDispatch = new _NotifyDispatch();
		m_Notify = new ArrayList<>();

		m_FileSystem = Jimfs.newFileSystem(m_sConfiguration);
		try {
			m_RAFOwnerPrincipal = m_FileSystem.getUserPrincipalLookupService().lookupPrincipalByName("internal");
		} catch(IOException e) {
			// Will never happen
			System.exit(1);
			throw new RuntimeException();
		}
		m_VersionData = new HashMap<>();

		m_DataFiles = new HashMap<>();
	}

	private static final Configuration m_sConfiguration = Configuration.builder(PathType.unix())
			.setRoots("/")
			.setWorkingDirectory("/")
			.setNameCanonicalNormalization(PathNormalization.CASE_FOLD_ASCII)
			.setPathEqualityUsesCanonicalForm(true)
			.setAttributeViews("posix")
			.setSupportedFeatures(Feature.FILE_CHANNEL)
			.build();

	/**
	 * Add a new notify handler. This causes the the onAdd() function in the handler to be called for every node in the
	 * tree.
	 *
	 * @param ion The notify handler.
	 */
	public void addNotifyHandler(IOperationsNotify ion) {
		if(ion == null || m_Notify.contains(ion)) {
			return;
		}

		/* We're a new handler, so we don't know the existing
		 * tree structure. Let's rebuild it for them! */
		for(Path root : m_FileSystem.getRootDirectories()) {
			try {
				_reprocess(root, ion);
			} catch(IOException e) {
				// Will never happen
			}
		}

		m_Notify.add(ion);
	}

	/**
	 * Simulates the rebuilding the tree.
	 *
	 * @param n The root node.
	 * @param notify The notify handler.
	 */
	private void _reprocess(Path path, IOperationsNotify notify) throws IOException {
		notify.onAdd(path);

		try(DirectoryStream<Path> children = Files.newDirectoryStream(path)) {
			for(Path child : children) {
				if(!Files.isDirectory(path)) {
					continue;
				}

				_reprocess(child, notify);
			}
		}
	}

	public void addFile(Path raf, String version) throws IOException {
		Path dat = Paths.get(String.format("%s.dat", raf.toString()));
		addFile(raf, dat, version);
	}

	public void writeToArchive(Path raf) throws IOException {
		//SeekableByteChannel bc = Files.newByteChannel(raf, StandardOpenOption.CREATE);

		List<Path> files = new ArrayList<>();

		Files.walkFileTree(this.getRoot(), new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				files.add(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				throw exc;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

		});

		try(FileOutputStream os = new FileOutputStream(raf.toFile())) {
			MappedByteBuffer buffer;

			/* Map the file into memory */
			try(FileChannel fChannel = os.getChannel()) {
				buffer = fChannel.map(FileChannel.MapMode.READ_WRITE, 0, fChannel.size());
				buffer.order(ByteOrder.LITTLE_ENDIAN);
			}

			buffer.putInt(RAFIDX_MAGIC);
			buffer.putInt(1);
			buffer.putInt(0);

			// List offset, TODO
			buffer.putInt(0);

			buffer.putInt(buffer.position() + 4);
			writeStringTable(files, buffer);
		}
	}

	private void writeStringTable(List<Path> paths, ByteBuffer bb) throws IOException {
		int dataSize = 4 + (paths.size() * 8);

		List<byte[]> pathBytes = new ArrayList<>(paths.size());
		for(Path p : paths) {
			byte[] bytes = p.toString().getBytes("US-ASCII");
			dataSize += bytes.length;
			pathBytes.add(bytes);
		}

		bb.putInt(dataSize);
		bb.putInt(paths.size());

		int stringOffset = pathBytes.size() * 8;
		for(byte[] b : pathBytes) {
			bb.putInt(stringOffset);
			bb.putInt(b.length);
			stringOffset += b.length;
		}

		for(byte[] b : pathBytes) {
			bb.put(b);
		}

	}

	/**
	 * Add a RAF Archive to the VFS.
	 *
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
			try(FileChannel fChannel = rfis.getChannel()) {
				buffer = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size());
				buffer.order(ByteOrder.LITTLE_ENDIAN);
			}

			/* Check the magic number */
			if((magic = buffer.getInt()) != RAFIDX_MAGIC) {
				throw new IOException(String.format("%s: Invalid magic number. Expected 0x%X, got 0x%X\n", versionName, RAFIDX_MAGIC, magic));
			}

			/* Make sure we're version 1 */
			if((version = buffer.getInt()) != 1) {
				throw new IOException(String.format("%s: Unsupported version %d\n", versionName, version));
			}

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
			List<Path> indexMap = new ArrayList<>(st.size());

			for(final String s : st) {

				Path path = this.getRoot().resolve(s);

				Path parent = path.getParent();
				try {
					Files.createDirectories(parent);
				} catch(FileAlreadyExistsException e) {
				}

				if(!Files.exists(path)) {
					Files.createFile(path);
					PosixFileAttributeView a = Files.getFileAttributeView(path, PosixFileAttributeView.class);
					a.setOwner(m_RAFOwnerPrincipal);

					m_NotifyDispatch.onAdd(path);
				}

				indexMap.add(path);
			}

			/* Read the file list */
			buffer.position(lOffset);
			readFileList(buffer, indexMap, st, versionName, dat);
		}
	}

	public boolean isInRAF(Path path) {
		if(path.getFileSystem().equals(m_FileSystem)) {
			return false;
		}

		try {
			PosixFileAttributeView a = Files.getFileAttributeView(path, PosixFileAttributeView.class);
			return a.getOwner() == m_RAFOwnerPrincipal;
		} catch(IOException e) {
			return false;
		}
	}

	public Path getRoot() {
		return m_FileSystem.getPath("/");
	}

	public FileSystem getFileSystem() {
		return m_FileSystem;
	}

	public UserPrincipal getOwnerPrincipal() {
		return m_RAFOwnerPrincipal;
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
	 *
	 * @param b The ByteBuffer containing the data. Is expected to be at the position where the file table starts.
	 * @param indexMap The mapping of string table indices to their FileNodes.
	 * @param rawPaths The list of raw paths (using the same indices as indexMap).
	 * @param version The version of files in the file table. Should be of the form "X.X.X.X"
	 * @param dat The path to the data file.
	 * @throws IOException If an I/O error occurred.
	 */
	private void readFileList(ByteBuffer b, List<Path> indexMap, List<String> rawPaths, String version, Path dat) throws IOException {
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

			Path fn = indexMap.get(index);

			/* If we've already got this file loaded, don't load it again */
			PossiblyZippedSynchronisedFile rdf;
			if(m_DataFiles.containsKey(dat)) {
				rdf = m_DataFiles.get(dat);
			} else {
				m_DataFiles.put(dat, (rdf = new PossiblyZippedSynchronisedFile(dat)));
			}

			addVersionForFile(fn, version, rdf.createDataSource(offset, size));
		}
	}

	void addVersionForFile(Path path, String version, DataSource ds) {
		if(version == null || version.isEmpty() || ds == null) {
			throw new IllegalArgumentException();
		}

		TreeSet<Version> versions = m_VersionData.getOrDefault(path, null);

		if(versions == null) {
			m_VersionData.put(path, (versions = new TreeSet<>(new VSort())));
		}

		Version v = new Version(version, ds);

		if(versions.contains(v)) {
			throw new IllegalArgumentException(String.format("Duplicate version %s for %s\n", version, path));
		}

		versions.add(v);
	}

	public Version getVersionDataForFile(Path path, String version) throws IOException {
		if(path == null) {
			throw new IllegalArgumentException();
		}

		if(Files.isDirectory(path)) {
			throw new IOException(String.format("%s is a directory", path));
		}

		TreeSet<Version> vers = m_VersionData.getOrDefault(path, null);
		if(vers == null) {
			return null;
		}

		if(version == null) {
			return vers.last();
		}

		for(Version v : vers) {
			if(v.versionCompare(version)) {
				return v;
			}
		}

		throw new IOException(String.format("No such version %s for file %s\n", version, path));
	}

	public Set<Version> getFileVersions(Path path) {
		return Collections.unmodifiableSet(m_VersionData.getOrDefault(path, new TreeSet<>()));
	}

	public void clear() {
		m_NotifyDispatch.onClear();

		try {
			Files.walkFileTree(this.getRoot(), new RecursiveDeleteWalker());
		} catch(IOException e) {

		}
	}

	public void dumpPaths(PrintStream stream) throws IOException {
		Files.walkFileTree(this.getRoot(), new DumpWalker(stream));
	}

	private void _dumpPaths(Path path, PrintStream stream, int tab) throws IOException {

		for(int i = 0; i < tab; ++i) {
			stream.print("  ");
		}

		stream.printf("%s\n", path.getFileName());

		if(!Files.isDirectory(path)) {
//			for(final FileNode.Version v : fn.getVersions()) {
//				for(int i = 0; i <= tab; ++i) {
//					stream.print("  ");
//				}
//
//				stream.printf("V: %s\n", v.toString());
//			}
		} else {
			try(DirectoryStream<Path> children = Files.newDirectoryStream(path)) {
				for(final Path p : children) {
					_dumpPaths(path, stream, tab + 1);
				}
			}
		}
	}

	/**
	 * Extract a file or directory from the VFS.
	 *
	 * @param vfsPath The path to extract.
	 * @param outPath The output file/directory.
	 * @param version The version of the file to extract. If vfsPath is a directory, then this parameter is ignored.
	 * @throws IOException If an I/O error occurs.
	 */
	public void extract(Path vfsPath, Path outPath, String version) throws IOException {

		if(vfsPath == null) {
			throw new IllegalArgumentException("vfsPath cannot be null");
		}

		if(outPath == null) {
			throw new IllegalArgumentException("outPath cannot be null");
		}

		if(!Files.exists(vfsPath)) {
			throw new IOException("No such file or directory");
		}

		Files.walkFileTree(vfsPath, new ExtractWalker(outPath, version, this, m_NotifyDispatch));

		m_NotifyDispatch.onComplete();
	}

	/**
	 * Dump the entire RAF FileSystem to a folder.
	 *
	 * @param dir The base directory to write everything to.
	 * @throws IOException If an I/O error occurred.
	 */
	public void dumpToDir(String dir) throws IOException {
		extract(this.getRoot(), Paths.get(dir), null);
	}

	/**
	 * Fire a completion event.
	 */
	public void fireCompletion() {
		m_NotifyDispatch.onComplete();
	}

	/**
	 * Read the string table.
	 *
	 * @param b The ByteBuffer containing the data. Is expected to be at the position where the string table starts.
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

			byte[] tmp = new byte[idx[i].size - 1];
			b.get(tmp, 0, idx[i].size - 1);

			/* Are they US-ASCII? They seem to be for now */
			list.add(new String(tmp, Charset.forName("US-ASCII")));
		}

		return list;
	}

	private static class VSort implements Comparator<Version> {

		private final IPv4Sorter m_Sorter;

		public VSort() {
			m_Sorter = new IPv4Sorter();
		}

		@Override
		public int compare(Version v1, Version v2) {
			return m_Sorter.compare(v1.version, v2.version);
		}
	}

	private class _NotifyDispatch implements IOperationsNotify {

		@Override
		public void onClear() {
			m_Notify.stream().forEach((ion) -> {
				ion.onClear();
			});
		}

		@Override
		public void onAdd(Path n) {
			m_Notify.stream().forEach((ion) -> {
				ion.onAdd(n);
			});
		}

		@Override
		public void onExtract(Path n) {
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
