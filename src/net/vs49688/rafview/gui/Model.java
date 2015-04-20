package net.vs49688.rafview.gui;

import java.io.*;
import java.nio.file.*;
import net.vs49688.rafview.vfs.*;

public class Model {
	private RAFS m_VFS;
	
	public Model() {
		m_VFS = new RAFS();
	}
	
	public void addFile(Path file) throws IOException {
		Path dat = Paths.get(file.getParent().toString(), String.format("%s.dat", file.getFileName().toString()));
		m_VFS.addFile(file, dat);
	}
	
}
