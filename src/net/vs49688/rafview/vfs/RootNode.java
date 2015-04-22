package net.vs49688.rafview.vfs;

public class RootNode extends DirNode {

	public RootNode(IOperationsNotify notify) {
		super(notify);
	}
	
	@Override
	public String toString() {
		return "/";
	}
}
