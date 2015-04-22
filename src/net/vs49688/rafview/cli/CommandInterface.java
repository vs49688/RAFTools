package net.vs49688.rafview.cli;

import javax.swing.SwingUtilities;
import net.vs49688.rafview.interpreter.*;
import net.vs49688.rafview.cli.commands.*;

public class CommandInterface {
	
	private Appendable m_Out;
	private final Interpreter m_Interpreter;
	private final Model m_Model;
	private IFuckedUp m_ExtFuckupHandler;
	
	ICommand m_ShowCommand;
	ICommand m_OpenCommand;
	ICommand m_AddCommand;
	ICommand m_OpenDirCommand;
	ICommand m_ExtractCommand;
	
	public CommandInterface(Appendable out, Model model) {
		m_Out = out;
		m_ExtFuckupHandler = null;
		m_Model = model;
		m_Interpreter = new Interpreter(new _CommandError());
		
		m_Interpreter.registerCommand((m_ShowCommand = new Show(out)));
		m_Interpreter.registerCommand((m_OpenCommand = new Open(out, model)));
		m_Interpreter.registerCommand((m_AddCommand = new Add(out, model)));
		m_Interpreter.registerCommand((m_OpenDirCommand = new OpenDir(out, model)));
		m_Interpreter.registerCommand((m_ExtractCommand = new Extract(out, model)));
		printGPL();
	}
	
	private void printGPL() {
		_write("RAFTools v0.1 - Copyright (C) 2015 Zane van Iperen\n");
		_write("RAFTools comes with ABSOLUTELY NO WARRANTY; for details\n");
		_write("type `show w'.  This is free software, and you are welcome\n");
		_write("to redistribute it under certain conditions; type `show c' \n");
		_write("for details.\n");
	}
	
	public void setFuckupHandler(IFuckedUp handler) {
		m_ExtFuckupHandler = handler;
	}
	
	public void start() {
		m_Interpreter.start();
	}
	
	public void stop() {
		m_Interpreter.stop();
	}
	
	public void setOutputStream(Appendable s) {
		if(s == null)
			throw new IllegalArgumentException("s cannot be null");
		
		m_Out = s;
	}
	
	private void _write(String s) {
		try {
			m_Out.append(s);
		} catch(Exception e) {}
	}
	
	/**
	 * Parse and execute a string.
	 * @param s The string to parse
	 * @return 
	 */
	public Interpreter.CommandResult parseString(String s) {
		System.err.printf("> %s\n", s);
		return m_Interpreter.executeCommand(s);
	}
	
	class _CommandError implements IFuckedUp {
		@Override
		public void onFuckup(Interpreter.CommandResult result) {
			SwingUtilities.invokeLater(() -> {
				_write((String)result.getException().getMessage());
				_write("\n");
			});
			
			if(m_ExtFuckupHandler != null)
				m_ExtFuckupHandler.onFuckup(result);
		}
	}
}
