package net.vs49688.rafview.vfs;
import java.nio.file.*;
import java.util.Objects;

public abstract class Node {
	private static int s_NextUID = 0;
	
	private synchronized static int _getNextUID() {
		return s_NextUID++;
	}
	
    private String m_Name;
	private final int m_UID;
    private DirNode m_Parent;
	private Object m_UserObject;
	protected final IOperationsNotify m_Notify;
	
	public Node(IOperationsNotify notify) {
		m_Name = "";
		m_Parent = null;
		m_UID = _getNextUID();
		m_Notify = notify;
	}
	
	public Node(String name, IOperationsNotify notify) {
		this(notify);
		
        if(!isNameValid(name))
            throw new IllegalArgumentException("Invalid name");
        
        m_Name = name;
	}
	
    public final synchronized String name() {
        return m_Name;
    }
    
    public final synchronized void rename(String name) {
        if(!isNameValid(name))
            throw new IllegalArgumentException("Invalid name");
        
        m_Name = name;
		
		m_Notify.onModify(this);
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
	
	public void setUserObject(Object o) {
		m_UserObject = o;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 71 * hash + Objects.hashCode(this.m_Name);
		hash = 71 * hash + this.m_UID;
		hash = 71 * hash + Objects.hashCode(this.m_Parent);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Node other = (Node) obj;
		if (!Objects.equals(this.m_Name, other.m_Name)) {
			return false;
		}
		if (this.m_UID != other.m_UID) {
			return false;
		}
		if (!Objects.equals(this.m_Parent, other.m_Parent)) {
			return false;
		}
		return true;
	}
	
	public Object getUserObject() {
		return m_UserObject;
	}

}
