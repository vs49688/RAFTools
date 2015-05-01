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
	ICommand m_RamInfoCommand;
	ICommand m_ForceGC;
	Help m_HelpCommand;
	
	public CommandInterface(Appendable out, Model model) {
		m_Out = out;
		m_ExtFuckupHandler = null;
		m_Model = model;
		m_Interpreter = new Interpreter(new _CommandError());
		
		//m_Interpreter.registerCommand((m_ShowCommand = new Show(out)));
		m_Interpreter.registerCommand((m_OpenCommand = new Open(out, model)));
		m_Interpreter.registerCommand((m_AddCommand = new Add(out, model)));
		m_Interpreter.registerCommand((m_OpenDirCommand = new OpenDir(out, model)));
		m_Interpreter.registerCommand((m_ExtractCommand = new Extract(out, model)));
		m_Interpreter.registerCommand((m_RamInfoCommand = new RamInfo(out)));
		m_Interpreter.registerCommand((m_ForceGC = new ForceGC()));
		
		m_HelpCommand = new Help(out);
		m_HelpCommand.addHandler(m_HelpCommand);
		m_HelpCommand.addHandler(m_OpenCommand);
		m_HelpCommand.addHandler(m_AddCommand);
		m_HelpCommand.addHandler(m_OpenDirCommand);
		m_HelpCommand.addHandler(m_ExtractCommand);
		m_HelpCommand.addHandler(m_RamInfoCommand);
		m_HelpCommand.addHandler(m_ForceGC);
		
		m_Interpreter.registerCommand(m_HelpCommand);
		
		printGPL();
	}
	
	private void printGPL() {
		
		_write(String.format("%s %s - Copyright (C) %d %s\n",
			Model.getApplicationName(),	Model.getVersionString(),
			Model.getCopyrightYear(), Model.getCopyrightHolder()));
		_write(String.format("    Contact: %s\n", Model.getContactEmail()));
		//_write(String.format("This version of %s is a preview build for users of\n", Model.getApplicationName()));
		//_write("/r/leagueoflegends (And Rito if they pls).\n");
		_write(String.format("RAFTools comes with ABSOLUTELY NO WARRANTY, to the extent\n", Model.getApplicationName()));
		_write("permitted by applicable law. This build is an alpha-quality\n");
		_write("release and should not be considered stable enough for everyday\nuse.\n");
		
		_write("Type `help' for more information.\n");

		//_write("type `show w'.  This is free software, and you are welcome\n");
		//_write("to redistribute it under certain conditions; type `show c' \n");
		//_write("for details.\n");
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
		System.err.printf("PS: %s\n", s);
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
