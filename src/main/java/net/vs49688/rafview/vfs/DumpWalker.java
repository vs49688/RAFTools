package net.vs49688.rafview.vfs;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import static java.nio.file.FileVisitResult.*;
import java.nio.file.attribute.BasicFileAttributes;

public class DumpWalker implements FileVisitor<Path> {

	private final PrintStream m_Stream;
	private int m_NumTabs;
	
	public DumpWalker(PrintStream stream) {
		m_Stream = stream;
		m_NumTabs = 0;
	}
	
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		m_Stream.printf("%s\n", dir.getFileName());
		++m_NumTabs;
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		printTabs();
		m_Stream.printf("%s\n", file.getFileName());
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		printTabs();
		m_Stream.printf("RecursiveDeleteWalker::visitFileFailed(%s): %s\n", file, exc.getMessage());
		return CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		--m_NumTabs;
		return CONTINUE;
	}
	
	private void printTabs() {
		for(int i = 0; i < m_NumTabs; ++i) {
			m_Stream.print("  ");
		}
	}
	
}
