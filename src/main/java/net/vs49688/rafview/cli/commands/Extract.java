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
import java.nio.file.*;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.interpreter.*;

public class Extract implements ICommand {

	private final Model m_Model;
	private final PrintStream m_Console;
	
	public Extract(PrintStream out, Model model) {
		m_Console = out;
		m_Model = model;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		if(args.length < 3)
			throw new CommandException(cmdLine, getUsageString());
		
		Path outPath = Paths.get(args[args.length-1]);
		for(int i = 1; i < args.length-1; ++i) {
			String version;
			String file[] = args[i].trim().split(":", 2);
			if(file.length == 1) {
				version = null;
			} else {
				version = file[1];
			}
			
			Path vfsPath = m_Model.getVFS().getFileSystem().getPath(file[0]);
			if(!vfsPath.isAbsolute()) {
				vfsPath = m_Model.getCurrentDirectory().resolve(vfsPath).normalize();
			}
			
			m_Model.getVFS().extract(vfsPath, outPath, version);			
		}
	}

	@Override
	public String getCommand() {
		return "extract";
	}

	@Override
	public String getUsageString() {
		return "file1[:version]... [file2[:version]... [...]] output_directory";
	}
	
	@Override
	public String getDescription() {
		return "Extract a file or directory";
	}
}
