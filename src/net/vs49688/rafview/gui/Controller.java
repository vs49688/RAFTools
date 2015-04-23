package net.vs49688.rafview.gui;

import net.vs49688.rafview.cli.Model;
import java.awt.event.*;
import java.io.*;
import javax.swing.SwingUtilities;
import net.vs49688.rafview.vfs.*;
import net.vs49688.rafview.cli.*;
import net.vs49688.rafview.interpreter.IFuckedUp;
import net.vs49688.rafview.interpreter.Interpreter;

public class Controller {
	private final View m_View;
	private final Model m_Model;
	private final CommandInterface m_CLI;
	private final AboutDialog m_AboutDialog;
	
	public Controller() {
		m_Model = new Model();
		m_View = new View(m_Model, new MenuListener(), new _TreeOpHandler());
		m_AboutDialog = new AboutDialog(m_View);
		m_CLI = new CommandInterface(m_View.getConsole(), m_Model);
	
		m_CLI.setFuckupHandler(new _FuckupHandler());
		
		m_View.invokeLater();
		
		SwingUtilities.invokeLater(() -> { m_CLI.start(); });
	}

	private class MenuListener implements ActionListener {
		@Override
		@SuppressWarnings("ConvertToStringSwitch")
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			
			//System.err.printf("Got %s\n", cmd);
			
			if(cmd.equals("file->exit")) {
				m_CLI.stop();
				m_View.setVisible(false);
				m_View.dispose();
			} else if(cmd.equals("file->openarchive")) {
				File f = m_View.showOpenDialog(false);
				if(f != null)
					m_CLI.parseString(String.format("open \"%s\"", f.toString()));
			} else if(cmd.equals("file->addarchive")) {
				File f = m_View.showOpenDialog(false);
				if(f != null)
					m_CLI.parseString(String.format("add \"%s\"", f.toString()));
			} else if(cmd.equals("file->openlol")) {
				File f = m_View.showOpenDialog(true);
				if(f != null)
					m_CLI.parseString(String.format("opendir \"%s\"", f.toString()));
			} else if(cmd.equals("console->submit")) {				
				m_CLI.parseString(m_View.getConsole().getCommandText());
			} else if(cmd.equals("help->about")) {
				m_AboutDialog.setVisible(true);
			}
		}
	}

	private class _TreeOpHandler implements VFSViewTree.OpHandler {

		@Override
		public void nodeSelected(Node node) {
			m_View.setPathText(node.getFullPath().toString());
		}

		@Override
		public void nodeExport(Node node) {
			File f;
			if(node instanceof DirNode)
				f = m_View.showSaveDialog("", true);
			else
				f = m_View.showSaveDialog(node.name(), true);
			
			if(f == null)
				return;
			
			m_CLI.parseString(String.format("extract \"%s\" \"%s\"", node.getFullPath(), f.toString()));
		}
		
	}
	
	private class _FuckupHandler implements IFuckedUp {

		@Override
		public void onFuckup(Interpreter.CommandResult result) {
			m_View.showErrorDialog("ERROR", result.getException().getMessage());
		}
	
	}
}
