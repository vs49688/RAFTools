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

import net.vs49688.rafview.cli.Model;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.tree.*;
import net.vs49688.rafview.vfs.*;

public class View extends JFrame {

	public static final int FILETYPE_DIR	= (1 << 0);
	public static final int FILETYPE_RAF	= (1 << 1);
	public static final int FILETYPE_INIBIN	= (1 << 2);
	public static final int FILETYPE_DDS	= (1 << 3);
	public static final int FILETYPE_PNG	= (1 << 4);
	public static final int FILETYPE_ALL	= (1 << 5);

	private final Model m_Model;
	private final VFSViewTree.OpHandler m_TreeOpHandler;
	
	private String m_LastOpenDirectory;
	
	public View(Model model, ActionListener listener, VFSViewTree.OpHandler treeOpHandler) {
		m_Model = model;
		m_TreeOpHandler = treeOpHandler;
		
		m_LastOpenDirectory = System.getProperty("user.home");

		/* EDIT: No, the system look and feel on Windows is horrible.
		 * Attempt to set the system look and feel. It doesn't really matter
		 * if this fails. */
		/*
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(ReflectiveOperationException|UnsupportedLookAndFeelException e) {
			
		}*/

		initComponents();
		
		m_OpenArchive.addActionListener(listener);
		m_AddArchive.addActionListener(listener);
		m_OpenDir.addActionListener(listener);
		m_Exit.addActionListener(listener);
		m_About.addActionListener(listener);
		
		this.setTitle(String.format("%s %s", Model.getApplicationName(), Model.getVersionString()));
		this.setSize(700, 500);
		
		m_VFSTree.setOperationsHandler(m_TreeOpHandler);
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		setStatus("");
		/* This will cause onAdd() to be called for the root */
		m_Model.getVFS().addNotifyHandler(new _NotifyHandler());
	}
	
	private MutableTreeNode _tepkek(Node node) {
		
		DefaultMutableTreeNode tn = new DefaultMutableTreeNode(node);		
		node.setUserObject(tn);

		if(node instanceof DirNode) {
			DirNode dn = (DirNode)node;
			
			for(final Node n: dn)
				tn.add(_tepkek(n));

		} else if(node instanceof FileNode) {
			FileNode fn = (FileNode)node;
		}
		
		return tn;
	}

	public void addTab(Component tab, String name) {
		m_TabPane.addTab(name, tab);
	}
	
	public void setPathText(String path) {
		if(path == null)
			path = "";
		
		m_PathField.setText(path);
	}

    /**
     * Show the Open File Dialog.
	 * @param typeFlags The selection flags.
	 * The FILETYPE_DIR flag, if set, tells the dialog to only allow
	 * directory selection. Otherwise, the flags shall be made up of a
	 * combination of the FILETYPE_RAF and FILETYPE_INIBIN flags.
     * @return The selected file, or null if the dialog was canceled.
     */
    public File showOpenDialog(int typeFlags)
    {
        JFileChooser fc = new JFileChooser(m_LastOpenDirectory);
        fc.setMultiSelectionEnabled(false);

		addFileFilters(fc, typeFlags);

        /* If we've cancelled, do nothing. */
        if(fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
             return null;
        
        File f = fc.getSelectedFile();
        m_LastOpenDirectory = f.getParent();
        
        return f;
    }

	
    /**
     * Show the Save File Dialog.
	 * @param defaultName The default file name.
	 * @param typeFlags The selection flags.
	 * The FILETYPE_DIR flag, if set, tells the dialog to only allow
	 * directory selection. Otherwise, the flags shall be made up of a
	 * combination of the FILETYPE_RAF and FILETYPE_INIBIN flags.
     * @return The selected file, or null if the dialog was canceled.
     */
    public File showSaveDialog(String defaultName, int typeFlags)
    {
        JFileChooser fc = new JFileChooser(m_LastOpenDirectory);
		
		if((typeFlags & FILETYPE_DIR) == 0) {
			fc.setSelectedFile(new File(defaultName));
		}
		
		addFileFilters(fc, typeFlags);

        /* If we've cancelled, do nothing. */
        if(fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
             return null;
        
        File f = fc.getSelectedFile();
        m_LastOpenDirectory = f.getParent();
        
        return f;
    }

	private static void addFileFilters(JFileChooser fc, int typeFlags) {
		if((typeFlags & FILETYPE_DIR) == 0) {
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			if((typeFlags & FILETYPE_RAF) != 0)
				fc.addChoosableFileFilter(new FileNameExtensionFilter("RAF Index (.raf)", "raf"));

			if((typeFlags & FILETYPE_INIBIN) != 0) {
				fc.addChoosableFileFilter(new FileNameExtensionFilter("Binary INI File (.inibin, .troybin)", "inibin", "troybin"));
				fc.addChoosableFileFilter(new FileNameExtensionFilter("Binary INI File (.inibin)", "inibin"));
				fc.addChoosableFileFilter(new FileNameExtensionFilter("Binary INI File (.troybin)", "troybin"));
			}
			
			if((typeFlags & FILETYPE_DDS) != 0) {
				fc.addChoosableFileFilter(new FileNameExtensionFilter("DirectDraw Surface (.dds)", "dds"));
			}

			if((typeFlags & FILETYPE_PNG) != 0) {
				fc.addChoosableFileFilter(new FileNameExtensionFilter("Portable Network Graphics (.png)", "png"));
			}
			
			fc.setAcceptAllFileFilterUsed((typeFlags & FILETYPE_ALL) != 0);
		} else {
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}	
	}
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JSplitPane mainInfoSplitter = new javax.swing.JSplitPane();
        javax.swing.JSplitPane treePreviewSplitter = new javax.swing.JSplitPane();
        javax.swing.JScrollPane vfsScroll = new javax.swing.JScrollPane();
        m_VFSTree = new net.vs49688.rafview.gui.VFSViewTree();
        m_TabPane = new javax.swing.JTabbedPane();
        m_InfoPanel = new javax.swing.JPanel();
        m_PathField = new javax.swing.JTextField();
        javax.swing.JLabel pathLabel = new javax.swing.JLabel();
        m_StatusLabel = new javax.swing.JLabel();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        m_OpenArchive = new javax.swing.JMenuItem();
        m_AddArchive = new javax.swing.JMenuItem();
        m_OpenDir = new javax.swing.JMenuItem();
        m_Exit = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        m_About = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(700, 500));

        mainInfoSplitter.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainInfoSplitter.setResizeWeight(1.0);

        vfsScroll.setMinimumSize(new java.awt.Dimension(128, 23));

        m_VFSTree.setModel(null);
        vfsScroll.setViewportView(m_VFSTree);

        treePreviewSplitter.setLeftComponent(vfsScroll);
        treePreviewSplitter.setRightComponent(m_TabPane);

        mainInfoSplitter.setTopComponent(treePreviewSplitter);

        m_InfoPanel.setMinimumSize(new java.awt.Dimension(0, 64));
        m_InfoPanel.setPreferredSize(new java.awt.Dimension(598, 64));
        m_InfoPanel.setRequestFocusEnabled(false);

        m_PathField.setEditable(false);

        pathLabel.setText("Path:");

        m_StatusLabel.setText("<STATUS>");
        m_StatusLabel.setToolTipText("");

        javax.swing.GroupLayout m_InfoPanelLayout = new javax.swing.GroupLayout(m_InfoPanel);
        m_InfoPanel.setLayout(m_InfoPanelLayout);
        m_InfoPanelLayout.setHorizontalGroup(
            m_InfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(m_InfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(m_InfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, m_InfoPanelLayout.createSequentialGroup()
                        .addComponent(pathLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_PathField, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE))
                    .addGroup(m_InfoPanelLayout.createSequentialGroup()
                        .addComponent(m_StatusLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        m_InfoPanelLayout.setVerticalGroup(
            m_InfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(m_InfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(m_InfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathLabel)
                    .addComponent(m_PathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addComponent(m_StatusLabel)
                .addContainerGap())
        );

        mainInfoSplitter.setRightComponent(m_InfoPanel);

        fileMenu.setText("File");

        m_OpenArchive.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        m_OpenArchive.setText("Open Archive");
        m_OpenArchive.setActionCommand("file->openarchive");
        fileMenu.add(m_OpenArchive);

        m_AddArchive.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        m_AddArchive.setText("Add Archive");
        m_AddArchive.setActionCommand("file->addarchive");
        fileMenu.add(m_AddArchive);

        m_OpenDir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        m_OpenDir.setText("Open LoL Directory");
        m_OpenDir.setActionCommand("file->openlol");
        fileMenu.add(m_OpenDir);

        m_Exit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        m_Exit.setText("Exit");
        m_Exit.setActionCommand("file->exit");
        fileMenu.add(m_Exit);

        menuBar.add(fileMenu);

        helpMenu.setText("Help");

        m_About.setText("About");
        m_About.setActionCommand("help->about");
        helpMenu.add(m_About);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainInfoSplitter)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainInfoSplitter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	public void invokeLater() {
		SwingUtilities.invokeLater(() -> {
			View.this.setVisible(true);
		});
	}
	
	public void setStatus(String status) {
		if(status == null)
			status = "";
		
		m_StatusLabel.setText(status);
	}
	
    public void showErrorDialog(String title, String message)
    {
        showErrorDialog(this, title, message);
    }

    public static void showErrorDialog(Component parent, String title, String message)
    {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
	
	private class _NotifyHandler implements IOperationsNotify {

		@Override
		public void onClear() {

			DefaultMutableTreeNode root = (DefaultMutableTreeNode)m_Model.getVFS().getRoot().getUserObject();
			
			if(root.getChildCount() > 0) {
				DefaultTreeModel model = (DefaultTreeModel)m_VFSTree.getModel();
				for(int i = 0; i < root.getChildCount(); ++i)
					model.removeNodeFromParent((DefaultMutableTreeNode)root.getLastChild());
			}
		}

		@Override
		public void onModify(Node n) {
		}

		@Override
		public void onAdd(Node n) {
			DefaultMutableTreeNode tn = new DefaultMutableTreeNode(n);
			n.setUserObject(tn);
			
			DirNode parent = n.getParent();
			
			if(parent != null) {
				DefaultTreeModel m = (DefaultTreeModel)m_VFSTree.getModel();
				
				DefaultMutableTreeNode tnp = (DefaultMutableTreeNode)parent.getUserObject();
				m.insertNodeInto(tn, tnp, parent.getIndex(n));
				setStatus(String.format("Adding %s...", n.getFullPath().toString()));
			} else {
				m_VFSTree.setModel(new DefaultTreeModel(tn));
			}
		}
	}

	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem m_About;
    private javax.swing.JMenuItem m_AddArchive;
    private javax.swing.JMenuItem m_Exit;
    private javax.swing.JPanel m_InfoPanel;
    private javax.swing.JMenuItem m_OpenArchive;
    private javax.swing.JMenuItem m_OpenDir;
    private javax.swing.JTextField m_PathField;
    private javax.swing.JLabel m_StatusLabel;
    private javax.swing.JTabbedPane m_TabPane;
    private net.vs49688.rafview.gui.VFSViewTree m_VFSTree;
    // End of variables declaration//GEN-END:variables
}
