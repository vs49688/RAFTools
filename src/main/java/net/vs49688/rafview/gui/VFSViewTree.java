/*
 * RAFTools - Copyright (C) 2016 Zane van Iperen.
 *    Contact: zane@zanevaniperen.com
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
import java.io.IOException;
import java.nio.file.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import net.vs49688.rafview.vfs.*;

public class VFSViewTree extends JTree {

	private static final OpHandler s_DummyOpHandler = new OpHandler() {

		@Override
		public void nodeSelected(Path node) {
		}

		@Override
		public void nodeExport(Path node, Version version) {
		}
	};

	private OpHandler m_OpHandler;
	private RAFS m_FileSystem;

	public VFSViewTree() {
		super((DefaultTreeModel)null);
		this.addMouseListener(new _Mouse());
		this.addTreeSelectionListener(new _Selection());
		this.addTreeExpansionListener(new _Expansion());
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		m_OpHandler = s_DummyOpHandler;
		m_FileSystem = null;
	}

	private JPopupMenu createPopupMenu(Path n) {
		JPopupMenu m = new JPopupMenu();

		if(!Files.isDirectory(n)) {

			JMenu menu = new JMenu("Extract");

			JMenuItem item = new JMenuItem("Latest");
			item.addActionListener((ActionEvent ae) -> {
				try {
					m_OpHandler.nodeExport(n, m_FileSystem.getVersionDataForFile(n, null));
				} catch(IOException e) {
					// will never happen
				}
			});

			menu.add(item);

			for(final Version v : m_FileSystem.getFileVersions(n)) {
				item = new JMenuItem(v.toString());
				item.addActionListener((ActionEvent ae) -> {
					m_OpHandler.nodeExport(n, v);
				});
				menu.add(item);
			}
			m.add(menu);
		} else {
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

	public void setVFS(RAFS vfs) {
		if(m_FileSystem != null) {
			throw new IllegalStateException("vfs can only be set once");
		}

		m_FileSystem = vfs;

		DefaultTreeModel model = new DefaultTreeModel(new FSEntryNode(m_FileSystem.getRoot()));
		this.setModel(model);
	}

	private Path getSelectedVFSNode() {
		if(this.getSelectionCount() != 1) {
			return null;
		}

		return ((FSEntryNode)this.getLastSelectedPathComponent()).path;
	}

	private class _Mouse extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			int row = VFSViewTree.this.getClosestRowForLocation(e.getX(), e.getY());
			VFSViewTree.this.setSelectionRow(row);

			Path path = getSelectedVFSNode();

			if(SwingUtilities.isRightMouseButton(e)) {
				JPopupMenu m_ContextMenu = createPopupMenu(path);
				VFSViewTree.this.add(m_ContextMenu);
				m_ContextMenu.show(e.getComponent(), e.getX(), e.getY());

			}
		}
	}

	private class _Selection implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent tse) {
			Path p = getSelectedVFSNode();
			if(p != null) {
				m_OpHandler.nodeSelected(p);
			}
		}
	}

	public interface OpHandler {

		public void nodeSelected(Path node);

		public void nodeExport(Path node, Version version);
	}

	private static class _Expansion implements TreeExpansionListener {

		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			TreePath path = event.getPath();
			JTree tree = (JTree)event.getSource();

			FSEntryNode node = (FSEntryNode)path.getLastPathComponent();
			try {
				node.repopulate();
				((DefaultTreeModel)tree.getModel()).nodeStructureChanged(node);
			} catch(IOException e) {
				e.printStackTrace(System.err);
			}
		}

		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			/* nop */
		}
	}

	private static class FSEntryNode extends DefaultMutableTreeNode {

		public final Path path;
		public final boolean isDirectory;
		private final String m_FileName;

		public FSEntryNode(Path path) {
			this.path = path;
			isDirectory = Files.isDirectory(path);

			Path fn = path.getFileName();
			if(fn == null) {
				m_FileName = "/";
			} else {
				m_FileName = fn.toString();
			}
		}

		public void repopulate() throws IOException {
			this.removeAllChildren();

			try(DirectoryStream<Path> _children = Files.newDirectoryStream(path)) {
				for(Path child : _children) {
					this.add(new FSEntryNode(child));
				}
			}
		}

		@Override
		public boolean isLeaf() {
			return !isDirectory;
		}

		@Override
		public String toString() {
			return m_FileName;
		}
	}
}
