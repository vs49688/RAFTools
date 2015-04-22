package net.vs49688.rafview.cli.commands;

import net.vs49688.rafview.interpreter.*;

public class Show implements ICommand {
	private final Appendable m_Console;
	
	public Show(Appendable con) {
		m_Console = con;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		if(args.length != 2)
			throw new CommandException(cmdLine, "show: argument required\n");
		
		switch(args[1].toLowerCase()) {
			case "c":
				m_Console.append("show: TODO: Show conditions\n");
				break;
			case "w":
				m_Console.append("show: TODO: Show warranty information\n");
				break;
			default:
				throw new CommandException(cmdLine, "show: invalid argument");
		}
	}

	@Override
	public String getCommand() {
		return "show";
	}
	
}
