package net.vs49688.rafview.vfs;
import java.nio.file.*;

public abstract class Node {
	
	private static int s_NextUID = 0;
	
	private synchronized static int _getNextUID() {
		return s_NextUID++;
	}
	
    private String m_Name;
	private final int m_UID;
    
    private DirNode m_Parent;

	public Node() {
		m_Name = "";
		m_Parent = null;
		m_UID = _getNextUID();
	}
	
	public Node(String name) {
		this();
		rename(name);
	}
	
    public final synchronized String name() {
        return m_Name;
    }
    
    public final synchronized void rename(String name) {
        if(!isNameValid(name))
            throw new IllegalArgumentException("Invalid name");
        
        m_Name = name;
    }
	
	protected abstract void _delete();
	public final synchronized void delete() {
		if(m_Parent != null)
			throw new IllegalArgumentException("Cannot delete node with parent");

		_delete();
	}
	
	public abstract boolean isLeaf();
    
    public synchronized void setParent(DirNode parent) {
        m_Parent = parent;
    }
    
    public synchronized DirNode getParent() {
        return m_Parent;
    }

	public Path getFullPath() {
		
		Path p = Paths.get(name());
		
		if(m_Parent != null)
			p = m_Parent.getFullPath().resolve(p);
		
		return p;
	}

	public final int getUID() {
		return m_UID;
	}

    protected final boolean isNameValid(String s) {
        if(s == null)
            return false;
        
		if(s.contains("/"))
			return false;
		
		return !s.contains(":");
    }
	
	@Override
	public String toString() {
		return m_Name;
	}
}
