package net.vs49688.rafview.interpreter;

public class CommandException extends Exception {

	public CommandException(String cmdLine, String msg) {
		super(msg);
	}
}
