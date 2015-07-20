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

			JMenuItem item = new JMenuItem("Latest");
			item.addActionListener((ActionEvent ae) -> {
				m_OpHandler.nodeExport(n, fn.getLatestVersion());
			});
			
			menu.add(item);
			
			for(final FileNode.Version v : fn.getVersions()) {
				item = new JMenuItem(v.toString());
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
