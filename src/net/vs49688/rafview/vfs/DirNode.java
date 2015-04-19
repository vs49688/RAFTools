package net.vs49688.rafview.vfs;

import java.util.*;

public class DirNode extends Node implements Iterable<Node> {
	private final Set<Node> m_Children;
	
	public DirNode() {
		m_Children = new HashSet<>();
	}
	
	public DirNode(String name) {
		super(name);
		
		m_Children = new HashSet<>();
	}

	public synchronized Node AddChild(Node node) {
		if(node == null)
			throw new IllegalArgumentException("Cannot add null node");
		
		if(m_Children.contains(node))
			throw new IllegalArgumentException("Node already a child");
		
		m_Children.add(node);
		
		node.setParent(this);
		
		return node;
	}
	
	public DirNode getAddDirectory(String name){
		if(name == null || name.isEmpty() || !isNameValid(name))
			throw new IllegalArgumentException("Invalid directory name");
		
		for(Node n : this) {
			if(n.getName().equals(name)) {
				if(!(n instanceof DirNode))
					throw new IllegalArgumentException("File/Directory already exists");
				
				return (DirNode)n;
			}
		}

		return (DirNode)AddChild(new DirNode(name));
	}
	
	@Override
	public Iterator<Node> iterator() {
		return Collections.unmodifiableSet(m_Children).iterator();
	}
}
