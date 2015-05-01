package net.vs49688.rafview.gui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import net.vs49688.rafview.vfs.*;

public class VFSViewTree extends JTree {
	
	private static final OpHandler s_DummyOpHandler = new OpHandler() {

		@Override
		public void nodeSelected(Node node) { }

		@Override
		public void nodeExport(Node node, FileNode.Version version) { }
	};
	
	private OpHandler m_OpHandler;
	//private final JPopupMenu m_ContextMenu;

	public VFSViewTree() {
		super();
		this.addMouseListener(new _Mouse());
		this.addTreeSelectionListener(new _Selection());
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		m_OpHandler = s_DummyOpHandler;
	}
	
	private JPopupMenu createPopupMenu(Node n) {
		JPopupMenu m = new JPopupMenu();
		
		if(n instanceof FileNode) {
			FileNode fn = (FileNode)n;
			
			JMenu menu = new JMenu("Extract");

			menu.add(new JMenuItem("Latest"));
			menu.addActionListener((ActionEvent ae) -> {
				m_OpHandler.nodeExport(n, fn.getLatestVersion());
			});
			
			for(final FileNode.Version v : fn.getVersions()) {
				JMenuItem item = new JMenuItem(v.toString());
				item.addActionListener((ActionEvent ae) -> {
					m_OpHandler.nodeExport(n, v);
				});
				menu.add(item);
			}
			m.add(menu);
		} else if(n instanceof DirNode) {
			DirNode fn = (DirNode)n;
			
			JMenuItem item = new JMenuItem("Extract");
			item.addActionListener((ActionEvent ae) -> {
				m_OpHandler.nodeExport(n, null);
			});
			m.add(item);
		}
		
		return m;
	}
	
	public void setOperationsHandler(OpHandler handler) {
		m_OpHandler = handler == null ? s_DummyOpHandler : handler;
	}
	
	private Node getSelectedVFSNode() {
		if(this.getSelectionCount() != 1)
			return null;
		
		return (Node)((DefaultMutableTreeNode)this.getLastSelectedPathComponent()).getUserObject();
	}
	
	private class _Mouse implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			int row = VFSViewTree.this.getClosestRowForLocation(e.getX(), e.getY());
			VFSViewTree.this.setSelectionRow(row);

			Node vfsNode = getSelectedVFSNode();
			
			if(SwingUtilities.isRightMouseButton(e)) {
				JPopupMenu m_ContextMenu = createPopupMenu(vfsNode);
				VFSViewTree.this.add(m_ContextMenu);
				m_ContextMenu.show(e.getComponent(), e.getX(), e.getY());
				
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			
		}
	}
	
	private class _Selection implements TreeSelectionListener {	

		@Override
		public void valueChanged(TreeSelectionEvent tse) {
			Node n = getSelectedVFSNode();
			if(n != null)
				m_OpHandler.nodeSelected(n);
			
		}
	}
	
	public interface OpHandler {
		public void nodeSelected(Node node);
		public void nodeExport(Node node, FileNode.Version version);
	}
}
