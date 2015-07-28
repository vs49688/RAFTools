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
package net.vs49688.rafview.cli.commands.unix;

import java.io.PrintStream;
import java.nio.file.*;
import java.util.*;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.interpreter.*;
import net.vs49688.rafview.vfs.*;
import net.vs49688.rafview.vfs.FileNode.Version;

public class LS implements ICommand {

	private final Model m_Model;
	private final PrintStream m_Console;
	
	private static final int FLAG_LONG = (1 << 0);
	private static final int FLAG_ALLVER = (1 << 1);
	
	public LS(PrintStream out, Model model) {
		m_Console = out;
		m_Model = model;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		
		int flags = 0;
		
		List<String> paths = new ArrayList<>();
		for(int i = 1; i < args.length; ++i) {
			if(args[i].startsWith("-") && args[i].length() != 1) {
				flags |= parseFlags(cmdLine, args[i]);
				continue;
			}
			
			paths.add(args[i]);
		}
		
		if(paths.isEmpty()) {
			dumpPath(cmdLine, flags, m_Model.getCurrentDirectory());
		} else for(final String path : paths) {
			dumpPath(cmdLine, flags, path);
		}
		
	}
	
	private int parseFlags(String cmdLine, String s) throws CommandException {
		if(s.length() == 1)
			return 0;
		
		s = s.toLowerCase();
		int flags = 0;
		for(int i = 1; i < s.length(); ++i) {
			char c = s.charAt(i);
			if(c == 'l') {
				flags |= FLAG_LONG;
			} else if(c == 'v') {
				flags |= FLAG_ALLVER;
			} else {
				throw new CommandException(cmdLine, String.format("ls: invalid option -- '%c'", c));
			}
		}
		
		return flags;
	}
	
	private void dumpPath(String cmdLine, int flags, String path) throws CommandException {
		try {
			dumpPath(cmdLine, flags, Paths.get(path));
		} catch(InvalidPathException e) {
			m_Console.printf("ls: Cannot access %s: %s\n", e.toString());
		}
	}
	
	private void dumpPath(String cmdLine, int flags, Path path) throws CommandException {
		RAFS vfs = m_Model.getVFS();
		Node node = vfs.getNodeFromPath(path);
		
		if(node == null) {
			m_Console.printf("ls: Cannot access %s: No such file or directory\n", path);
			return;
		}

		dumpNode(flags, node);
	}
	
	private void dumpNode(int flags, Node node) {
		
		if((flags & FLAG_LONG) == 0) {
			if(node instanceof DirNode) {
				DirNode dn = (DirNode)node;
				
				for(final Node n : dn) {
					m_Console.printf("%s\n", n.name());
				}
			
			} else {
				m_Console.printf("%s\n", node.name());
			}
		
			return;
		}
		
		if(node instanceof FileNode) {
			printNode(node, flags);
		} else if(node instanceof DirNode) {
			DirNode dn = (DirNode)node;
			if(dn instanceof RootNode) {
				m_Console.printf("/:\n");
			} else {
				m_Console.printf("%s:\n", dn.getFullPath().toString());
			}
			m_Console.printf("total %d\n", dn.getChildCount());
			
			for(final Node n : dn) {
				printNode(n, flags);
			}
		}
	}

	private void printNode(Node node, int flags) {
		if(node instanceof FileNode) {
			FileNode fn = (FileNode)node;

			Set<Version> vers = new HashSet<>();
			if((flags & FLAG_ALLVER) != 0) {
				vers.addAll(fn.getVersions());
			} else {
				vers.add(fn.getLatestVersion());
			}

			for(final Version v : vers) {
				m_Console.printf("%-16s %s\n", v.toString(), fn.name());
			}
		} else if(node instanceof RootNode) {
			m_Console.printf("%-16s %s\n", "/", node.name());
		} else if(node instanceof DirNode) {
			m_Console.printf("%-16s %s\n", "", node.name());
		}
	}
	
	@Override
	public String getCommand() {
		return "ls";
	}

	@Override
	public String getUsageString() {
		return "[-l] [file]...";
	}

	@Override
	public String getDescription() {
		return "List directory contents";
	}
	
}
