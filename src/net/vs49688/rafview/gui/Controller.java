package net.vs49688.rafview.gui;

import java.awt.event.*;
import java.io.*;

public class Controller {
	private final View m_View;
	private final Model m_Model;
	
	public Controller() {
		m_Model = new Model();
		m_View = new View(m_Model, new MenuListener());
		
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
					System.err.printf("WTO %s\n", f.toString());
				}
					
			} else if(cmd.equals("file->openlol")) {
				File f = m_View.showOpenDialog(true);
				if(f != null) {
					
				}
			}
		}
	}
}
