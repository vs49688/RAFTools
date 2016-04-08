package net.vs49688.rafview.vfs;

import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.FileVisitResult.*;
import java.nio.file.attribute.BasicFileAttributes;

public class ExtractWalker implements FileVisitor<Path> {

	private final String m_Version;
	private final Path m_NativePath;
	private final Path m_CurrentDirectory;
	private final RAFS m_VFS;
	
	public ExtractWalker(Path nativePath, Path currentDirectory, String version, RAFS vfs) {
		m_Version = version;
		m_NativePath = nativePath;
		if(!Files.isDirectory(currentDirectory)) {
			m_CurrentDirectory = currentDirectory.getParent();
		} else {
			m_CurrentDirectory = currentDirectory;
		}
		m_VFS = vfs;
	}
	
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		Files.createDirectories(Paths.get(dir.toString()));
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Files.write(m_NativePath.resolve(m_CurrentDirectory.relativize(file).toString()), m_VFS.getVersionDataForFile(file, m_Version).dataSource.read());
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		System.err.printf("RecursiveDeleteWalker::visitFileFailed(%s): %s\n", file, exc.getMessage());
		exc.printStackTrace(System.err);
		return TERMINATE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		return CONTINUE;
	}
	
}
