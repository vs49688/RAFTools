package net.vs49688.rafview.cli.commands;

import net.vs49688.rafview.interpreter.*;

public class ForceGC implements ICommand {
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		Runtime.getRuntime().gc();
	}

	@Override
	public String getCommand() {
		return "forcegc";
	}
	
	@Override
	public String getUsageString() {
		return "";
	}
	
	@Override
	public String getDescription() {
		return "Force a Garbage Collection";
	}
}
