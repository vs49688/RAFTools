package net.vs49688.rafview.cli.commands.unix;

import java.io.PrintStream;
import java.nio.file.*;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.interpreter.*;
import net.vs49688.rafview.vfs.*;

public class CD implements ICommand {

	private final Model m_Model;
	private final PrintStream m_Console;
	
	public CD(PrintStream out, Model model) {
		m_Console = out;
		m_Model = model;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		if(args.length != 2)
			throw new CommandException(cmdLine, "cd: Invalid arguments");

		/* HACKHACKHACK: On Windows, paths starting with / are changed to start with \
		 * which are drive-relative, not absolute. The correct way to fix this is to
		 * create a proper java.nio.file.Path implementation for RAFS, but I really
		 * can't be bothered doing that. */
		boolean relative = (!args[1].startsWith("/") && !args[1].startsWith("\\"));
		
		try {
			Path path = Paths.get(args[1]).normalize();
			if(relative) {
				changeRelative(m_Model.getCurrentDirectory(), path);
			} else {
				changeRelative(m_Model.getVFS().getRoot().getFullPath(), path);
			}
		} catch(InvalidPathException e) {
			m_Console.printf("cd: %s: No such file or directory\n", args[1]);
		}
	}

	private void changeAbsolute(Path path) {
		RAFS vfs = m_Model.getVFS();
		
		Node newNode = vfs.getNodeFromPath(path);
		
		if(newNode == null) {
			m_Console.printf("cd: %s: No such file or directory\n", path.toString());
			return;
		}
		
		if(newNode instanceof FileNode) {
			m_Console.printf("cd: %s: Not a directory\n", path.toString());
			return;
		}
		
		m_Model.setCurrentDirectory(newNode.getFullPath());
	}
	
	private void changeRelative(Path root, Path path) {
		RAFS vfs = m_Model.getVFS();
		
		DirNode currentNode = (DirNode)vfs.getNodeFromPath(root);
		
		
		Node newNode = vfs.getNodeFromPath(currentNode, path);
		
		if(newNode == null) {
			m_Console.printf("cd: %s: No such file or directory\n", path.toString());
			return;
		}
		
		if(newNode instanceof FileNode) {
			m_Console.printf("cd: %s: Not a directory\n", path.toString());
			return;
		}
		
		m_Model.setCurrentDirectory(newNode.getFullPath());
	}

	
	@Override
	public String getCommand() {
		return "cd";
	}

	@Override
	public String getUsageString() {
		return "<directory>";
	}

	@Override
	public String getDescription() {
		return "change the working directory";
	}
	
}
