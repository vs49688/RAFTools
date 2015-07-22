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
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.text.ParseException;
import javax.swing.SwingUtilities;
import java.util.*;
import javax.imageio.ImageIO;
import net.vs49688.rafview.vfs.*;
import net.vs49688.rafview.cli.*;
import net.vs49688.rafview.inibin.*;
import net.vs49688.rafview.interpreter.*;
import net.vs49688.rafview.sources.DataSource;
import net.vs49688.rafview.wwise.Wwise;

public class Controller {
	private final View m_View;
	private final Model m_Model;
	private final CommandInterface m_CLI;
	private final AboutDialog m_AboutDialog;
	private final Console m_Console;
	private final InibinViewer m_InibinViewer;
	private final DDSViewer m_DDSViewer;
	private final VersionDialog m_VerDialog;
	private final WwiseViewer m_WwiseViewer;
	
	private final DelayLoader m_InibinLoader;
	private final DelayLoader m_DDSLoader;
	private final DelayLoader m_WwiseLoader;
	
	public Controller() {
		ActionListener al = new MenuListener();
		
		m_InibinLoader = new InibinDelayedLoader();
		m_DDSLoader = new DDSDelayedLoader();
		m_WwiseLoader = new WwiseDelayedLoader();
		
		m_Model = new Model();
		m_Model.getVFS().addNotifyHandler(new _TreeNotifyHandler());
		m_View = new View(m_Model, al, new _TreeOpHandler());
		
		m_Console = new Console(al);
		m_InibinViewer = new InibinViewer(al);
		m_DDSViewer = new DDSViewer(al);
		m_WwiseViewer = new WwiseViewer(al);
		
		m_View.addTab(m_Console, "Console");
		m_View.addTab(m_InibinViewer, "Inibin Viewer");
		m_View.addTab(m_DDSViewer, "DDS Viewer");
		m_View.addTab(m_WwiseViewer, "Wwise Viewer");
		
		m_AboutDialog = new AboutDialog(m_View);
		
		m_VerDialog = new VersionDialog(m_View);
		
		m_CLI = new CommandInterface(m_Console.getStream(), m_Model);
	
		m_CLI.setFuckupHandler(new _FuckupHandler());
		
		m_View.getRootPane().setDefaultButton(m_Console.getSubmitButton());
		
		m_View.invokeLater();
		
		SwingUtilities.invokeLater(() -> { m_CLI.start(); });
	}

	private class MenuListener implements ActionListener {
		@Override
		@SuppressWarnings("ConvertToStringSwitch")
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			
			//System.err.printf("Got %s\n", cmd);
			
			try {
				if(cmd.equals("file->exit")) {
					m_CLI.stop();
					m_View.setVisible(false);
					m_View.dispose();
				} else if(cmd.equals("file->openarchive")) {
					File f = m_View.showOpenDialog(View.FILETYPE_RAF);
					if(f != null) {			
						m_CLI.parseString(String.format("open \"%s\" \"%s\"", f.toString(), _getVersionFromDialog()));
					}
				} else if(cmd.equals("file->addarchive")) {
					File f = m_View.showOpenDialog(View.FILETYPE_RAF);
					if(f != null)
						m_CLI.parseString(String.format("add \"%s\" \"%s\"", f.toString(), _getVersionFromDialog()));
				} else if(cmd.equals("file->openlol")) {
					File f = m_View.showOpenDialog(View.FILETYPE_DIR);
					if(f != null)
						m_CLI.parseString(String.format("opendir \"%s\"", f.toString()));
				} else if(cmd.equals("console->submit")) {				
					m_CLI.parseString(m_Console.getCommandText());
				} else if(cmd.equals("help->about")) {
					m_AboutDialog.setVisible(true);
				} else if(cmd.equals("inibin->export")) {

				} else if(cmd.equals("inibin->loadexternal")) {
					m_InibinLoader.delayLoad(m_View.showOpenDialog(View.FILETYPE_INIBIN));
				} else if(cmd.equals("dds->loadexternal")) {
					m_DDSLoader.delayLoad(m_View.showOpenDialog(View.FILETYPE_DDS));
				} else if(cmd.equals("dds->export")) {
					File f = m_View.showSaveDialog(String.format("%s.png", m_DDSViewer.getDDSName()), View.FILETYPE_PNG);
					
					if(f != null) {
						BufferedImage img = m_DDSViewer.getCurrentImage();

						if(!ImageIO.write(img, "PNG", f)) {
							m_View.setStatus(String.format("Unexpected error writing %s", f.getName()));
						}
						
					}
				} else if(cmd.equals("wwise->loadexternal")) {
					m_WwiseLoader.delayLoad(m_View.showOpenDialog(View.FILETYPE_BNK));
				} 
			} catch(IOException ex) {
				m_View.showErrorDialog("ERROR", ex.getMessage());
			}
		}
	}

	private String _getVersionFromDialog() {
		String ver = "";
		
		while(ver == null || ver.isEmpty()) {
			m_VerDialog.setVisible(true);
			
			ver = m_VerDialog.getVersionText();
		}
		
		return ver;
	}
	
	private class _TreeOpHandler implements VFSViewTree.OpHandler {

		@Override
		public void nodeSelected(Node node) {
			Path fullPath = node.getFullPath();
			m_View.setPathText(fullPath.toString());
			
			if(node instanceof FileNode) {
				FileNode fn = (FileNode)node;
				
				if(node.name().toLowerCase().endsWith(".inibin") ||
					node.name().toLowerCase().endsWith(".troybin")) {
					m_InibinLoader.delayLoad(fn.name(), fn.getLatestVersion().getSource());
				} else if(node.name().toLowerCase().endsWith(".dds")) {
					m_DDSLoader.delayLoad(fn.name(), fn.getLatestVersion().getSource());
				} else if(node.name().toLowerCase().endsWith(".bnk")) {
					m_WwiseLoader.delayLoad(fn.name(), fn.getLatestVersion().getSource());
				}
			}
		}

		@Override
		public void nodeExport(Node node, FileNode.Version version) {
			File f;
			if(node instanceof DirNode)
				f = m_View.showSaveDialog("", View.FILETYPE_DIR);
			else
				f = m_View.showSaveDialog(node.name(), View.FILETYPE_ALL);
			
			if(f == null)
				return;
			
			String fn;
			
			if(version == null)
				fn = node.getFullPath().toString();
			else
				fn = String.format("%s:%s", node.getFullPath(), version);
			
			m_CLI.parseString(String.format("extract \"%s\" \"%s\"", fn, f.toString()));
		}
		
	}
	
	private class _FuckupHandler implements IFuckedUp {

		@Override
		public void onFuckup(Interpreter.CommandResult result) {
			result.getException().printStackTrace();
			m_View.showErrorDialog("ERROR", result.getException().getMessage());
		}
	}
	
	private class _TreeNotifyHandler implements IOperationsNotify {

		@Override
		public void onClear() {
			
		}

		@Override
		public void onModify(Node n) {
			
		}

		@Override
		public void onAdd(Node n) {
			if(n instanceof FileNode) {
				m_View.setStatus(String.format("Adding %s...", n.getFullPath().toString()));
			}
		}
		
		@Override
		public void onExtract(Node n) {
			if(n instanceof FileNode) {
				m_View.setStatus(String.format("Extracting %s...", n.getFullPath().toString()));
			}
		}
		
		@Override
		public void onComplete() {
			m_View.setStatus("Complete");
		}
		
	}
	
	private class WwiseDelayedLoader extends DelayLoader {
		@Override
		protected void load(String name, byte[] data) throws Exception {
			Wwise wwise = Wwise.load(data);
				
			SwingUtilities.invokeLater(() -> {
				m_WwiseViewer.setSoundbank(name, wwise);
			});
		}

		@Override
		protected void onException(Exception e) {
			SwingUtilities.invokeLater(() -> {
				m_View.showErrorDialog("ERROR", e.getMessage());
			});
		}
	};
	
	private class DDSDelayedLoader extends DelayLoader {
		@Override
		protected void load(String name, byte[] data) throws Exception {
			SwingUtilities.invokeLater(() -> {
					m_DDSViewer.setDDS(name, data);
			});
		}

		@Override
		protected void onException(Exception e) {
			SwingUtilities.invokeLater(() -> {
				m_View.showErrorDialog("ERROR", e.getMessage());
			});
		}
	};

	private class InibinDelayedLoader extends DelayLoader {

		@Override
		protected void load(String name, byte[] data) throws Exception {
			Map<Integer, Value> inibin = InibinReader.readInibin(data);

			SwingUtilities.invokeLater(() -> {
				m_InibinViewer.setInibin(inibin);
			});
		}

		@Override
		protected void onException(Exception e) {
			SwingUtilities.invokeLater(() -> {
				m_View.showErrorDialog("ERROR", e.getMessage());
			});
		}
	};
}