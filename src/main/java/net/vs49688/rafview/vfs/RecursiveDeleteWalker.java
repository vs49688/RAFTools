package net.vs49688.rafview.vfs;

import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.FileVisitResult.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveDeleteWalker implements FileVisitor<Path> {

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Files.delete(file);
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		System.err.printf("RecursiveDeleteWalker::visitFileFailed(%s): %s\n", file, exc.getMessage());
		exc.printStackTrace(System.err);
		return CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		Files.delete(dir);
		return CONTINUE;
	}

}
