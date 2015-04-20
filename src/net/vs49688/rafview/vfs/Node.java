package net.vs49688.rafview.vfs;

public abstract class Node {
	
	private static int s_NextUID = 0;
	
	private synchronized static int _getNextUID() {
		return s_NextUID++;
	}
	
    private String m_Name;
	private final int m_UID;
    
    private DirNode m_Parent;

	public Node() {
		m_Name = null;
		m_Parent = null;
		m_UID = _getNextUID();
	}
	
	public Node(String name) {
		this();
		setName(name);
	}
	
    public final synchronized String getName() {
        return m_Name;
    }
    
    public final synchronized void setName(String name) {
        if(!isNameValid(name))
            throw new IllegalArgumentException("Invalid name");
        
        m_Name = name;
    }
    
    
    public synchronized void setParent(DirNode parent) {
        m_Parent = parent;
    }
    
    public synchronized DirNode getParent() {
        return m_Parent;
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
		return m_Name == null ? "" : m_Name;
	}
}
