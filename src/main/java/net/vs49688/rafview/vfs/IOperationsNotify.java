package net.vs49688.rafview.vfs;

public interface IOperationsNotify {
	public void onClear();
	
	/**
	 * Called when a node has been modified.
	 * @param n The modified node.
	 */
	public void onModify(Node n);
	
	/**
	 * Called when a new node has been added to the tree.
	 * Because of the nature of a VFS, it is guaranteed that if this function
	 * is called for node n, then it has been called for n's parent.
	 * @param n The newly-added node.
	 */
	public void onAdd(Node n);
	//public void onDelete(Node n);
	
	//public void onOverwrite(Node n1, Node n2);
}
