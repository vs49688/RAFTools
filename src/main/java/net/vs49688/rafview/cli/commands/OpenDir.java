/*
 * RAFTools - Copyright (C) 2016 Zane van Iperen.
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
import java.nio.file.Paths;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.interpreter.*;

public class OpenDir implements ICommand {

	private final Model m_Model;
	private final PrintStream m_Console;
	
	public OpenDir(PrintStream out, Model model) {
		m_Console = out;
		m_Model = model;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		if(args.length != 2)
			throw new CommandException(cmdLine, "opendir: Invalid arguments");

		m_Model.openLolDirectory(Paths.get(args[1]));
		
		m_Console.printf("Opened LoL directory %s...\n", args[1]);
		
		m_Model.getVFS().fireCompletion();
	}

	@Override
	public String getCommand() {
		return "opendir";
	}
	
	@Override
	public String getUsageString() {
		return "path_to_lol_directory";
	}
	
	@Override
	public String getDescription() {
		return "Open a LoL directory";
	}
}
