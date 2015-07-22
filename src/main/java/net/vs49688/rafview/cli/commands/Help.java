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
package net.vs49688.rafview.cli.commands;

import java.io.*;
import net.vs49688.rafview.interpreter.*;
import java.util.*;

public class Help implements ICommand {
	private final PrintStream m_Console;
	private final List<ICommand> m_Handlers;
	
	public Help(PrintStream con) {
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
			m_Console.printf("%s %s\n", cmd.getCommand(), cmd.getUsageString());
			m_Console.printf("  %s\n", cmd.getDescription());
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
