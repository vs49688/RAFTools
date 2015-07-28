/*
 * RAFTools - Copyright (C) 2015 Zane van Iperen.
 *    Contact: zane.vaniperen@uqconnect.edu.au
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2, and only
 * version 2 as published by the Free Software Foundation. 
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Any and all GPL restrictions may be circumvented with permission from the
 * the original author.
 */
package net.vs49688.rafview.cli;

import javax.swing.SwingUtilities;
import java.io.*;
import net.vs49688.rafview.interpreter.*;
import net.vs49688.rafview.cli.commands.*;
import net.vs49688.rafview.cli.commands.unix.*;

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
	ICommand m_DumpCommand;
	
	ICommand m_UnixPwd;
	ICommand m_UnixLs;
	ICommand m_UnixCd;
	
	Help m_HelpCommand;
	
	public CommandInterface(PrintStream out, Model model) {
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
		m_Interpreter.registerCommand((m_DumpCommand = new Dump(out, model)));
		
		m_Interpreter.registerCommand((m_UnixPwd = new PWD(out, model)));
		m_Interpreter.registerCommand((m_UnixLs = new LS(out, model)));
		m_Interpreter.registerCommand((m_UnixCd = new CD(out, model)));
		
		m_HelpCommand = new Help(out);
		m_HelpCommand.addHandler(m_HelpCommand);
		m_HelpCommand.addHandler(m_OpenCommand);
		m_HelpCommand.addHandler(m_AddCommand);
		m_HelpCommand.addHandler(m_OpenDirCommand);
		m_HelpCommand.addHandler(m_ExtractCommand);
		m_HelpCommand.addHandler(m_RamInfoCommand);
		m_HelpCommand.addHandler(m_ForceGC);
		m_HelpCommand.addHandler(m_UnixPwd);
		m_HelpCommand.addHandler(m_UnixLs);
		m_HelpCommand.addHandler(m_UnixCd);
		
		m_Interpreter.registerCommand(m_HelpCommand);
		
		printGPL();
	}
	
	private void printGPL() {
		
		_write(String.format("%s %s - Copyright (C) %d %s\n",
			Model.getApplicationName(),	Model.getVersionString(),
			Model.getCopyrightYear(), Model.getCopyrightHolder()));
		_write(String.format("    Contact: %s\n", Model.getContactEmail()));

		/* Back in the day... :) */
		//_write(String.format("This version of %s is a preview build for users of\n", Model.getApplicationName()));
		//_write("/r/leagueoflegends (And Rito if they pls).\n");

		_write(String.format("%s comes with ABSOLUTELY NO WARRANTY, to the extent\n", Model.getApplicationName()));
		_write("permitted by applicable law. This is free software; see the\n");
		_write("source for copying conditions.\n");
		_write("This build is an alpha-quality release and should not be\n");
		_write("considered stable enough for everyday use.\n\n");
		
		_write("Type `help' for more information.\n");
		
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
			
			result.getException().printStackTrace(System.err);
		}
	}
}
