/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vs49688.rafview.gui;

import java.awt.*;
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
		public void nodeExport(Node node) { }
	};
	
	private OpHandler m_OpHandler;
	private final JPopupMenu m_ContextMenu;

	public VFSViewTree() {
		super();
		this.addMouseListener(new _Mouse());
		this.addTreeSelectionListener(new _Selection());
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		m_OpHandler = s_DummyOpHandler;
		m_ContextMenu = createPopupMenu();
		this.add(m_ContextMenu);
	}
	
	private JPopupMenu createPopupMenu() {
		JPopupMenu m = new JPopupMenu();
		
		JMenuItem item;
		
		item = new JMenuItem("Export");
		item.addActionListener((ActionEvent ae) -> {
			m_OpHandler.nodeExport(getSelectedVFSNode());
		});
		m.add(item);
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

			//Node vfsNode = getSelectedVFSNode();
			
			if(SwingUtilities.isRightMouseButton(e)) {
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
		public void nodeExport(Node node);
	}
}
