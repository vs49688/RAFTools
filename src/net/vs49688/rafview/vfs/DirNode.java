package net.vs49688.rafview.vfs;

import java.util.*;

public class DirNode extends Node implements Iterable<Node> {
	private final Set<Node> m_Children;

	public DirNode() {
		m_Children = createSet();
	}
	
	public DirNode(String name) {
		super(name);
		
		m_Children = createSet();
	}
	
	private static Set createSet() {
		return Collections.synchronizedSortedSet(new TreeSet<>((Node n1, Node n2) -> {
			int res = n1.name().compareTo(n2.name());
			return n1.name().compareTo(n2.name());
		}));
	}

	public synchronized Node addChild(Node node) {
		if(node == null)
			throw new IllegalArgumentException("Cannot add null node");
		
		if(m_Children.contains(node))
			throw new IllegalArgumentException("Node already a child");
		
		m_Children.add(node);
		
		node.setParent(this);
		
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

	public DirNode getAddDirectory(String name){
		if(name == null || name.isEmpty() || !isNameValid(name))
			throw new IllegalArgumentException("Invalid directory name");
		
		for(Node n : this) {
			if(n.name().equals(name)) {
				if(!(n instanceof DirNode))
					throw new IllegalArgumentException("File/Directory already exists");
				
				return (DirNode)n;
			}
		}

		return (DirNode)addChild(new DirNode(name));
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
