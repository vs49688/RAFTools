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

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.*;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.interpreter.*;
import net.vs49688.rafview.vfs.*;

public class LS implements ICommand {

	private final Model m_Model;
	private final PrintStream m_Console;

	private static final int FLAG_LONG = (1 << 0);
	private static final int FLAG_ALLVER = (1 << 1);

	public LS(PrintStream out, Model model) {
		m_Console = out;
		m_Model = model;
	}

	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {

		int flags = 0;

		List<String> paths = new ArrayList<>();
		for(int i = 1; i < args.length; ++i) {
			if(args[i].startsWith("-") && args[i].length() != 1) {
				flags |= parseFlags(cmdLine, args[i]);
				continue;
			}

			paths.add(args[i]);
		}

		if(paths.isEmpty()) {
			dumpPath(cmdLine, flags, m_Model.getCurrentDirectory());
		} else {
			for(final String path : paths) {
				dumpPath(cmdLine, flags, path);
			}
		}

	}

	private int parseFlags(String cmdLine, String s) throws CommandException {
		if(s.length() == 1) {
			return 0;
		}

		s = s.toLowerCase();
		int flags = 0;
		for(int i = 1; i < s.length(); ++i) {
			char c = s.charAt(i);
			if(c == 'l') {
				flags |= FLAG_LONG;
			} else if(c == 'v') {
				flags |= FLAG_ALLVER;
			} else {
				throw new CommandException(cmdLine, String.format("ls: invalid option -- '%c'", c));
			}
		}

		return flags;
	}

	private void dumpPath(String cmdLine, int flags, String path) throws CommandException, IOException {
		Path p = m_Model.getVFS().getFileSystem().getPath(path);

		if(!p.isAbsolute()) {
			p = m_Model.getCurrentDirectory().resolve(p);
		}

		try {
			dumpPath(cmdLine, flags, p);
		} catch(InvalidPathException e) {
			m_Console.printf("ls: Cannot access %s: %s\n", path, e.toString());
		}
	}

	private void dumpPath(String cmdLine, int flags, Path path) throws CommandException, IOException {
		if(!Files.exists(path)) {
			m_Console.printf("ls: Cannot access %s: No such file or directory\n", path);
			return;
		}

		dumpNode(flags, path);
	}

	private void dumpNode(int flags, Path path) throws IOException {

		boolean dir = Files.isDirectory(path);

		if(!dir) {
			printFile(path, dir, flags);
		} else {
			m_Console.printf("%s:\n", path);
			
			int totalFiles = 0;
			try(DirectoryStream<Path> children = Files.newDirectoryStream(path)) {
				for(Path child : children) {
					printFile(child, dir, flags);
					++totalFiles;
				}
			}
			
			m_Console.printf("total %d\n", totalFiles);

		}
	}

	private void printFile(Path path, boolean dir, int flags) throws IOException {
		if(!dir) {
			Set<Version> vers = new HashSet<>();
			
			if((flags & FLAG_ALLVER) != 0) {
				vers.addAll(m_Model.getVFS().getFileVersions(path));
			} else {
				vers.add(m_Model.getVFS().getVersionDataForFile(path, null));
			}
			
			for(final Version v : vers) {
				kek(path, flags, v.toString());
			}
		} else {
			kek(path, flags, "");
		}
	}
	
	private void kek(Path path, int flags, String version) {
		if((flags & FLAG_LONG) == 0) {
			m_Console.printf("%s\n", path.getFileName());
		} else {
			m_Console.printf("%-16s %s\n", version, path.getFileName());
		}
	}

	@Override
	public String getCommand() {
		return "ls";
	}

	@Override
	public String getUsageString() {
		return "[-l] [file]...";
	}

	@Override
	public String getDescription() {
		return "List directory contents";
	}

}
