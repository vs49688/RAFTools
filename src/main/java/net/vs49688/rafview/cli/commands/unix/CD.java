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

		try {
			Path path = Paths.get(args[1]);
			changeRelative(path);
		} catch(InvalidPathException e) {
			m_Console.printf("cd: %s: No such file or directory\n", args[1]);
		}
	}

	private void changeRelative(Path path) {
		RAFS vfs = m_Model.getVFS();
		Path cwd = m_Model.getCurrentDirectory();
		
		DirNode currentNode = (DirNode)vfs.getNodeFromPath(cwd);
		
		
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
