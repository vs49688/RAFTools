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

import java.util.*;

public class DirNode extends Node implements Iterable<Node> {
	private final Set<Node> m_Children;

	public DirNode(IOperationsNotify notify) {
		super(notify);
		m_Children = createSet();
	}
	
	public DirNode(String name, IOperationsNotify notify) {
		super(name, notify);
		
		m_Children = createSet();
	}
	
	private static Set<Node> createSet() {
		return Collections.synchronizedSortedSet(new TreeSet<>((Node n1, Node n2) -> {
			
			if(n1 instanceof DirNode && !(n2 instanceof DirNode)) {
				return 1;
			} else if(n2 instanceof DirNode && !(n1 instanceof DirNode)) {
				return -1;
			}
			
			return n1.name().compareToIgnoreCase(n2.name());
		}));
	}

	public Node addChild(Node node) {
		if(node == null)
			throw new IllegalArgumentException("Cannot add null node");
		
		m_Lock.writeLock().lock();
		try {
			// POTBUG: This doesn't seem to be working properly
			if(m_Children.contains(node)) {
				//throw new IllegalArgumentException("Node already a child");
				//System.err.printf("Duplicate node %s, skipping\n", node.getFullPath());
				return node;
			}
			
			return _addChild(node);
		
			/*for(final Node n : m_Children) {
				if(n.name().equalsIgnoreCase(node.name()))
					throw new IllegalArgumentException("Already a file with same name.");
			}*/
		} finally {
			m_Lock.writeLock().unlock();
		}
	}
	
	public synchronized Node _addChild(Node node) {
		m_Children.add(node);
		node.setParent(this);
		//System.err.printf("_addChild: %s\n", node.getFullPath());
		node.m_Notify.onAdd(node);
		return node;
	}
	
	public synchronized void deleteChild(Node node) {
		if(node == null)
			throw new IllegalArgumentException("Cannot delete null node");
		
		if(!m_Children.contains(node))
			throw new IllegalArgumentException("Node not child of parent");
		
		node.setParent(null);
		m_Children.remove(node);
	}

	private boolean kek(Node n1, Node n2) {
		//if(n1.getParent() != n2.getParent())
		//	return false;
		
		if(!n1.name().equalsIgnoreCase(n2.name()))
			return false;
		
		if((n1 instanceof DirNode) && !(n2 instanceof DirNode))
			return false;
		
		if((n1 instanceof FileNode) && !(n2 instanceof FileNode))
			return false;
		
		return true;
	}
	
	public Node existGetChild(String name) {
		m_Lock.readLock().lock();
		
		try {
			for(final Node n : m_Children) {
				if(n.name().equalsIgnoreCase(name))
					return n;
			}
		} finally {
			m_Lock.readLock().unlock();
		}
		
		return null;
	}
	
	public DirNode getAddDirectory(String name){
		if(name == null || name.isEmpty() || !isNameValid(name))
			throw new IllegalArgumentException("Invalid directory name");
		
		Node n = existGetChild(name);
		if(n == null)
			return (DirNode)addChild(new DirNode(name, m_Notify));
		
		if(!(n instanceof DirNode))
			throw new IllegalArgumentException("File exists");
		
		return (DirNode)n;
	}
	
	public int getChildCount() {
		return m_Children.size();
	}
	
	public int getIndex(Node child) {
		int i = 0;
		for(final Node n : m_Children) {
			if(child == n)
				return i;
			
			++i;
		}
		return -1;
	}
	
	@Override
	public Iterator<Node> iterator() {
		return Collections.unmodifiableSet(m_Children).iterator();
	}
	
	@Override
	protected void _delete() {
		for(final Node n : m_Children) {
			n.setParent(null);
			n.delete();
		}
		
		m_Children.clear();
	}
	
	@Override
	public boolean isLeaf() {
		return false;
	}
}
