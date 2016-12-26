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
import net.vs49688.rafview.interpreter.*;

public class RamInfo implements ICommand {

	private final PrintStream m_Console;
	
	public RamInfo(PrintStream out) {
		m_Console = out;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		long mb = 1048576;
		Runtime r = Runtime.getRuntime();
		
		m_Console.printf("Total Memory: %d MB\n", r.totalMemory() / mb);
		m_Console.printf("Free Memory:  %d MB\n", r.freeMemory() / mb);
		m_Console.printf("Used Memory:  %d MB\n", (r.totalMemory() - r.freeMemory()) / mb);
		m_Console.printf("Max Memory:   %d MB\n", r.maxMemory() / mb);
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
