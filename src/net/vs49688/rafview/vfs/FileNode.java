package net.vs49688.rafview.vfs;

import net.vs49688.rafview.sources.*;

public class FileNode extends Node {

	private DataSource m_DataSource;

	
    public FileNode(IOperationsNotify notify) {
        super(notify);
        m_DataSource = null;
    }
	
	public FileNode(String name, IOperationsNotify notify) {
		super(name, notify);
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
	
	@Override
	public boolean isLeaf() {
		return true;
	}
	
	@Override
	public synchronized void _delete() {
		if(m_DataSource != null)
			m_DataSource.close();
		
		m_DataSource = null;
	}
}
