package net.vs49688.rafview.cli.commands.unix;

import java.io.PrintStream;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.interpreter.*;

public class PWD implements ICommand {

	private final Model m_Model;
	private final PrintStream m_Console;
	
	public PWD(PrintStream out, Model model) {
		m_Console = out;
		m_Model = model;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		m_Console.printf("%s\n", m_Model.getCurrentDirectory());
	}

	@Override
	public String getCommand() {
		return "pwd";
	}

	@Override
	public String getUsageString() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Print name of current/working directory";
	}
	
}
