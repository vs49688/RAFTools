/*
 * RAFTools - Copyright (C) 2015 Zane van Iperen.
 *    Contact: zane.vaniperen@uqconnect.edu.au
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2, and only
 * version 2 as published by the Free Software Foundation. 
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Any and all GPL restrictions may be circumvented with permission from the
 * the original author.
 */
package net.vs49688.rafview.vfs;
import java.nio.file.*;
import java.util.Objects;
import java.util.concurrent.locks.*;

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
	protected final ReadWriteLock m_Lock;
	
	public Node(IOperationsNotify notify) {
		m_Name = "";
		m_Parent = null;
		m_UID = _getNextUID();
		m_Notify = notify;
		m_Lock = new ReentrantReadWriteLock();
	}
	
	public Node(String name, IOperationsNotify notify) {
		this(notify);
		
        if(!isNameValid(name))
            throw new IllegalArgumentException("Invalid name");
        
        m_Name = name;
	}
	
    public final String name() {
		m_Lock.readLock().lock();
		try {
			return m_Name;
		} finally {
			m_Lock.readLock().unlock();
		}
    }
    
    public final void rename(String name) {
        if(!isNameValid(name))
            throw new IllegalArgumentException("Invalid name");
        
		m_Lock.writeLock().lock();
		try {
			m_Name = name;
		} finally {
			m_Lock.writeLock().unlock();
		}
		
		m_Notify.onModify(this);
    }
	
	protected abstract void _delete();
	
	public final void delete() {
		m_Lock.writeLock().lock();
		try {
			if(m_Parent != null)
				throw new IllegalArgumentException("Cannot delete node with parent");

			_delete();
		} finally {
			m_Lock.writeLock().unlock();
		}
	}
	
	public abstract boolean isLeaf();
    
    public void setParent(DirNode parent) {
		m_Lock.writeLock().lock();
		try {
			m_Parent = parent;
		} finally {
			m_Lock.writeLock().unlock();
		}
    }
    
    public DirNode getParent() {
		m_Lock.readLock().lock();
        try {
			return m_Parent;
		} finally {
			m_Lock.readLock().unlock();
		}
    }

	public Path getFullPath() {
		
		m_Lock.readLock().lock();
		try {
			Path p = Paths.get(name());
			if(m_Parent != null)
				p = m_Parent.getFullPath().resolve(p);
			
			return p;
		} finally {
			m_Lock.readLock().unlock();
		}
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

	public Object getUserObject() {
		return m_UserObject;
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
}
