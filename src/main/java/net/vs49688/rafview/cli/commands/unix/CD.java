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
package net.vs49688.rafview.cli.commands.unix;

import java.io.PrintStream;
import java.nio.file.*;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.interpreter.*;
import net.vs49688.rafview.vfs.*;

public class CD implements ICommand {

	private final Model m_Model;
	private final PrintStream m_Console;
	
	public CD(PrintStream out, Model model) {
		m_Console = out;
		m_Model = model;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		if(args.length != 2)
			throw new CommandException(cmdLine, "cd: Invalid arguments");

		Path path = m_Model.getVFS().getFileSystem().getPath(args[1]);

		if(!path.isAbsolute()) {
			path = m_Model.getCurrentDirectory().resolve(path).normalize();
		}
		if(!Files.exists(path)) {
			m_Console.printf("cd: %s: No such file or directory\n", args[1]);
			return;
		}
		
		if(!Files.isDirectory(path)) {
			m_Console.printf("cd: %s: Not a directory\n", path.toString());
			return;
		}
			
		m_Model.setCurrentDirectory(path);		
	}

	@Override
	public String getCommand() {
		return "cd";
	}

	@Override
	public String getUsageString() {
		return "<directory>";
	}

	@Override
	public String getDescription() {
		return "change the working directory";
	}
	
}
