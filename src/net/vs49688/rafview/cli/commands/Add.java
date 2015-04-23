package net.vs49688.rafview.cli.commands;

import java.nio.file.Paths;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.interpreter.*;

public class Add implements ICommand {

	Model m_Model;
	Appendable m_Console;
	
	public Add(Appendable out, Model model) {
		m_Console = out;
		m_Model = model;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		if(args.length != 2)
			throw new CommandException(cmdLine, "add: Invalid arguments");
		m_Model.addFile(Paths.get(args[1]));
		
		m_Console.append(String.format("Added %s...\n", args[1]));
	}

	@Override
	public String getCommand() {
		return "add";
	}

	@Override
	public String getUsageString() {
		return "path_to_raf_file";
	}
	
	@Override
	public String getDescription() {
		return "Add a file to the VFS";
	}
}
