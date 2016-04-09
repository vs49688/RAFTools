/*
 * RAFTools - Copyright (C) 2015 Zane van Iperen.
 *    Contact: zane@zanevaniperen.com
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

public class Show implements ICommand {
	private final PrintStream m_Console;
	
	public Show(PrintStream con) {
		m_Console = con;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		if(args.length != 2)
			throw new CommandException(cmdLine, "show: <c|w>\n");
		
		switch(args[1].toLowerCase()) {
			case "c":
				m_Console.printf("This version of RAFTools is a preview build for\n");
				m_Console.printf("the users of /r/leagueoflegends. It is an alpha-quality\n");
				m_Console.printf("release and should not be considered stable for everyday\n");
				m_Console.printf("use.\n");
				break;
			case "w":
				m_Console.printf("show: TODO: Show warranty information\n");
				break;
			default:
				throw new CommandException(cmdLine, "show: invalid argument");
		}
	}

	@Override
	public String getCommand() {
		return "show";
	}
	
	@Override
	public String getUsageString() {
		return "";
	}
	
	@Override
	public String getDescription() {
		return "";
	}
}
