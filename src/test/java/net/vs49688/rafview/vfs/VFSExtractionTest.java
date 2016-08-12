package net.vs49688.rafview.vfs;

import java.io.*;
import java.nio.file.*;
import net.vs49688.rafview.sources.DataSource;
import org.junit.*;
import org.junit.rules.*;

public class VFSExtractionTest {

	private static void populateFileSystem(RAFS vfs) throws IOException {
		FileSystem fs = vfs.getFileSystem();

		Path root = fs.getPath("/");
		Path asheDir = root.resolve("DATA/Characters");

		Files.createDirectories(asheDir);

		Path testFile = asheDir.resolve("ashe.txt");
		Files.createFile(testFile);

		vfs.addVersionForFile(testFile, "0.0.0.0", new DataSource() {
			@Override
			public byte[] read() throws IOException {
				return "I am a file! Hear me roar!\n".getBytes();
			}

			@Override
			public void close() {

			}
		});
	}

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void extractRoot() throws Exception {
		RAFS vfs = new RAFS();
		populateFileSystem(vfs);
		vfs.extract(vfs.getFileSystem().getPath("/"), tempFolder.getRoot().toPath(), null);
	}

	@Test
	public void extractFolder() throws Exception {
		RAFS vfs = new RAFS();
		populateFileSystem(vfs);
		vfs.extract(vfs.getFileSystem().getPath("/DATA/Characters"), tempFolder.getRoot().toPath(), null);
	}

	@Test
	public void extractFileToDirectory() throws Exception {
		RAFS vfs = new RAFS();
		populateFileSystem(vfs);
		vfs.extract(vfs.getFileSystem().getPath("/DATA/Characters/ashe.txt"), tempFolder.getRoot().toPath(), null);
	}

	@Test
	public void extractFileToFile() throws Exception {
		RAFS vfs = new RAFS();
		populateFileSystem(vfs);
		vfs.extract(vfs.getFileSystem().getPath("/DATA/Characters/ashe.txt"), tempFolder.getRoot().toPath().resolve("ashe.txt"), null);
	}
}
