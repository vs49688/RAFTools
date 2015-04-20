package net.vs49688.rafview.gui;

import java.awt.event.*;
import java.io.*;
import net.vs49688.rafview.vfs.Node;

public class Controller {
	private final View m_View;
	private final Model m_Model;
	
	public Controller() {
		m_Model = new Model();
		m_View = new View(m_Model, new MenuListener(), new TreeOpHandler());
		
		m_View.invokeLater();
	}

	private class MenuListener implements ActionListener {
		@Override
		@SuppressWarnings("ConvertToStringSwitch")
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			
			System.err.printf("Got %s\n", cmd);
			
			if(cmd.equals("file->exit")) {
				m_View.setVisible(false);
				m_View.dispose();
			} else if(cmd.equals("file->openarchive")) {
				File f = m_View.showOpenDialog(false);
				if(f != null) {							
					try {
						m_Model.getVFS().clear();
						m_Model.addFile(f.toPath());
					} catch(IOException ex) {
						m_View.showErrorDialog("ERROR", ex.getMessage());
					}

					m_View.updateView();
				}
					
			} else if(cmd.equals("file->addarchive")) {
				File f = m_View.showOpenDialog(false);
				if(f != null) {							
					try {
						m_Model.addFile(f.toPath());
					} catch(IOException ex) {
						m_View.showErrorDialog("ERROR", ex.getMessage());
					}

					m_View.updateView();
				}
			} else if(cmd.equals("file->openlol")) {
				File f = m_View.showOpenDialog(true);
				if(f != null) {							
					try {
						m_Model.openLolDirectory(f.toPath());
					} catch(IOException ex) {
						m_View.showErrorDialog("ERROR", ex.getMessage());
					}

					m_View.updateView();
				}
			}
		}
	}
	
	private class TreeOpHandler implements VFSViewTree.OpHandler {

		@Override
		public void nodeSelected(Node node) {
			m_View.setPathText(node.getFullPath().toString());
		}

		@Override
		public void nodeExport(Node node) {	}
		
	}
}
