package net.vs49688.rafview.cli.commands;

import net.vs49688.rafview.interpreter.*;

public class RamInfo implements ICommand {

	private final Appendable m_Console;
	
	public RamInfo(Appendable out) {
		m_Console = out;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		long mb = 1048576;
		Runtime r = Runtime.getRuntime();
		
		m_Console.append(String.format("Total Memory: %d MB\n", r.totalMemory() / mb));
		m_Console.append(String.format("Free Memory:  %d MB\n", r.freeMemory() / mb));
		m_Console.append(String.format("Used Memory:  %d MB\n", (r.totalMemory() - r.freeMemory()) / mb));
		m_Console.append(String.format("Max Memory:   %d MB\n", r.maxMemory() / mb));
	}

	@Override
	public String getCommand() {
		return "raminfo";
	}
	
	@Override
	public String getUsageString() {
		return "";
	}
	
	@Override
	public String getDescription() {
		return "Show the system's current memory usage";
	}
}
