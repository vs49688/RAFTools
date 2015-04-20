package net.vs49688.rafview.vfs;

import net.vs49688.rafview.sources.*;

public class FileNode extends Node {

	private DataSource m_DataSource;

	
    public FileNode() {
        super();
        m_DataSource = null;
    }
	
	public FileNode(String name) {
		super(name);
		m_DataSource = null;
	}

	public void setSource(DataSource ds) {
		if(ds == null)
			throw new IllegalArgumentException();
		
		m_DataSource = ds;
	}
	
	public DataSource getSource() {
		return m_DataSource;
	}
}
