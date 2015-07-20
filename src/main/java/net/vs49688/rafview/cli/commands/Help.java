package net.vs49688.rafview.cli.commands;

import net.vs49688.rafview.interpreter.*;
import java.util.*;

public class Help implements ICommand {
	private final Appendable m_Console;
	private final List<ICommand> m_Handlers;
	
	public Help(Appendable con) {
		m_Console = con;
		m_Handlers = new LinkedList<>();
	}
	
	public void addHandler(ICommand cmd) {
		if(cmd == null)
			return;
		
		m_Handlers.add(cmd);
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		m_Console.append("Available commands:\n");
		
		for(final ICommand cmd : m_Handlers) {
			m_Console.append(String.format("%s %s\n", cmd.getCommand(), cmd.getUsageString()));
			m_Console.append(String.format("  %s\n", cmd.getDescription()));
		}
	}

	@Override
	public String getCommand() {
		return "help";
	}
	
	@Override
	public String getUsageString() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Show this message";
	}
}
