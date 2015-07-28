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
import org.ini4j.Ini;

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
	private final DelayLoader m_InibinMapLoader;
	private final DelayLoader m_DDSLoader;
	private final DelayLoader m_WwiseLoader;
	private final DelayWriter m_DelayWriter;
	
	public Controller() {
		ActionListener al = new MenuListener();
		
		m_InibinLoader = new InibinDelayedLoader();
		m_InibinMapLoader = new InibinMapDelayedLoader();
		m_DDSLoader = new DDSDelayedLoader();
		m_WwiseLoader = new WwiseDelayedLoader();
		m_DelayWriter = new _DelayWriter();
		
		m_Model = new Model();
		m_Model.getVFS().addNotifyHandler(new _TreeNotifyHandler());
		m_View = new View(m_Model, al, new _TreeOpHandler());
		
		m_Console = new Console(al);
		m_InibinViewer = new InibinViewer(al);
		m_DDSViewer = new DDSViewer(al);
		m_WwiseViewer = new WwiseViewer(al, new _WwiseNotifyHandler());
		
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
			} else if(cmd.equals("inibin->loadmappings")) {
				m_InibinMapLoader.delayLoad(m_View.showOpenDialog(View.FILETYPE_INI));
			} else if(cmd.equals("dds->loadexternal")) {
				m_DDSLoader.delayLoad(m_View.showOpenDialog(View.FILETYPE_DDS));
			} else if(cmd.equals("dds->export")) {
				File f = m_View.showSaveDialog(String.format("%s.png", m_DDSViewer.getDDSName()), View.FILETYPE_PNG);

				if(f != null) {
					m_DelayWriter.delayWriteImage(m_DDSViewer.getCurrentImage(), "PNG", f);
				}
			} else if(cmd.equals("wwise->loadexternal")) {
				m_WwiseLoader.delayLoad(m_View.showOpenDialog(View.FILETYPE_BNK));
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
			m_View.showErrorDialog("ERROR", result.getException().getMessage());
			result.getException().printStackTrace(System.err);
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
	
	private class _WwiseNotifyHandler implements WwiseViewer.OpHandler {

		@Override
		public void onSelect(Long id, Wwise wwise) {
			//System.err.printf("selected %d\n", id);
		}

		@Override
		public void onExtract(Long id, Wwise wwise) {
			File f = m_View.showSaveDialog(String.format("%d.wem", id), View.FILETYPE_WEM);
			
			if(f == null)
				return;
			
			m_DelayWriter.delayWrite(f.toPath(), wwise.getWEMs().get(id));
		}

		@Override
		public void onExtractAll(Wwise wwise) {
			File f = m_View.showSaveDialog("", View.FILETYPE_DIR);
			
			if(f == null)
				return;
			
			Map<Long, DataSource> wems = wwise.getWEMs();
			
			for(final Long id : wems.keySet()) {
				m_DelayWriter.delayWrite(f.toPath().resolve(String.format("%d.wem", id)), wems.get(id));
			}
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
				e.printStackTrace(System.err);
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
				e.printStackTrace(System.err);
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
				e.printStackTrace(System.err);
			});
		}
	};
	
	private class InibinMapDelayedLoader extends DelayLoader {

		@Override
		protected void load(String name, byte[] data) throws Exception {
			Map<String, Map<Integer, String>> outMap = new HashMap<>();
			
			Ini ini = new Ini();
			ini.load(new ByteArrayInputStream(data));

			outMap.put("Data", parseInibinKeys(ini.get("Data")));
			outMap.put("SpellData", parseInibinKeys(ini.get("SpellData")));
			outMap.put("BuffData", parseInibinKeys(ini.get("BuffData")));
			
			m_InibinViewer.setKeyMappings(outMap);
		}

		@Override
		protected void onException(Exception e) {
			SwingUtilities.invokeLater(() -> {
				m_View.showErrorDialog("ERROR", e.getMessage());
				e.printStackTrace(System.err);
			});
		}
	};
	
	private class _DelayWriter extends DelayWriter {
		@Override
		protected void onException(Exception e) {
			SwingUtilities.invokeLater(() -> {
				m_View.showErrorDialog("ERROR", e.getMessage());
				e.printStackTrace(System.err);
			});
		}
	};
	
	private static Map<Integer, String> parseInibinKeys(Ini.Section section) {
		Map<Integer, String> outMap = new HashMap<>();
		
		if(section == null)
			return outMap;

		
		for(final String s : section.keySet()) {
			int key;
			
			try {
				key = Integer.parseInt(section.get(s));
			} catch(NumberFormatException e) {
				// Invalid key, skip.
				continue;
			}
			
			outMap.put(key, s);
		}
		return outMap;
	}
	
	private static Map<String, Map<Integer, String>> loadInibinKeys(String name) throws IOException {
		Ini ini = new Ini();
		
		Map<String, Map<Integer, String>> outMap = new HashMap<>();
		try(FileReader r = new FileReader(new File(name))) {
			ini.load(r);
			
			outMap.put("Data", parseInibinKeys(ini.get("Data")));
			outMap.put("SpellData", parseInibinKeys(ini.get("SpellData")));
			outMap.put("BuffData", parseInibinKeys(ini.get("BuffData")));			
		}
		
		return outMap;
	}
}
