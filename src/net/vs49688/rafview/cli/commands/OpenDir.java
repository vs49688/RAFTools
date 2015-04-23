package net.vs49688.rafview.cli.commands;

import java.nio.file.Paths;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.interpreter.*;

public class OpenDir implements ICommand {

	Model m_Model;
	Appendable m_Console;
	
	public OpenDir(Appendable out, Model model) {
		m_Console = out;
		m_Model = model;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		if(args.length != 2)
			throw new CommandException(cmdLine, "opendir: Invalid arguments");

		m_Model.openLolDirectory(Paths.get(args[1]));
		
		m_Console.append(String.format("Opened LoL directory %s...\n", args[1]));
	}

	@Override
	public String getCommand() {
		return "opendir";
	}
	
	@Override
	public String getUsageString() {
		return "path_to_lol_directory";
	}
	
	@Override
	public String getDescription() {
		return "Open a LoL directory";
	}
}
