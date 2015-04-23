package net.vs49688.rafview.cli.commands;

import java.nio.file.Paths;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.interpreter.*;

public class Open implements ICommand {

	Model m_Model;
	Appendable m_Console;
	
	public Open(Appendable out, Model model) {
		m_Console = out;
		m_Model = model;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		if(args.length != 2)
			throw new CommandException(cmdLine, "open: Invalid arguments");
		m_Model.getVFS().clear();
		m_Model.addFile(Paths.get(args[1]));
		
		m_Console.append(String.format("Opened %s...\n", args[1]));
	}

	@Override
	public String getCommand() {
		return "open";
	}

	@Override
	public String getUsageString() {
		return "path_to_raf_file";
	}
	
	@Override
	public String getDescription() {
		return "Create a new VFS using a file as the base";
	}
}
