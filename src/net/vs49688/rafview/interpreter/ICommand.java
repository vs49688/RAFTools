package net.vs49688.rafview.interpreter;

public interface ICommand {

	public void process(String cmdLine, String[] args) throws CommandException, Exception;

	public String getCommand();
}
