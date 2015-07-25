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
			lel(cmdLine, flags, m_Model.getCurrentDirectory());
		} else for(final String path : paths) {
			lel(cmdLine, flags, path);
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
	
	private void lel(String cmdLine, int flags, String path) throws CommandException {
		try {
			lel(cmdLine, flags, Paths.get(path));
		} catch(InvalidPathException e) {
			m_Console.printf("ls: Cannot access %s: %s\n", e.toString());
		}
	}
	
	private void lel(String cmdLine, int flags, Path path) throws CommandException {
		RAFS vfs = m_Model.getVFS();
		Node node = vfs.getNodeFromPath(path);
		
		if(node == null) {
			m_Console.printf("ls: Cannot access %s: No such file or directory\n", path);
			return;
		}

		lel(cmdLine, flags, node);
	}
	
	private void lel(String cmdLine, int flags, Node node) {
		
		if((flags & FLAG_LONG) == 0) {
			m_Console.printf("%s\n", node.name());
			return;
		}
		
		if(node instanceof FileNode) {
			printNode(node, flags);
		} else if(node instanceof DirNode) {
			DirNode dn = (DirNode)node;
			if(dn instanceof RootNode) {
				m_Console.printf("/:\n");
			} else {
				m_Console.printf("%s:\n", dn.name());
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
