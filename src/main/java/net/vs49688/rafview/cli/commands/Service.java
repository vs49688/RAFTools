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
import java.util.Arrays;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.interpreter.*;
import org.apache.catalina.LifecycleException;

public class Service implements ICommand {

	private final Model m_Model;
	private final PrintStream m_Console;

	private enum Command {
		START,
		STOP,
		SET
	}

	public Service(PrintStream out, Model model) {
		m_Console = out;
		m_Model = model;
	}

	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		if(args.length < 3) {
			throw new CommandException(cmdLine, "service: Invalid arguments");
		}

		String service = args[1].toLowerCase();

		Command command = parseCommand(args[2]);
		if(command == null) {
			throw new CommandException(cmdLine, "Invalid command, expected [start, stop, set]");
		}

		String[] commandArgs = Arrays.copyOfRange(args, 3, args.length);

		if(service.equals("webdav")) {
			parseWebDAV(cmdLine, command, commandArgs);
		} else {
			throw new CommandException(cmdLine, "Invalid service name");
		}
	}

	private void parseWebDAV(String cmdLine, Command command, String[] commandArgs) throws CommandException {
		try {
			if(command == Command.START) {
				m_Model.startServer();
			} else if(command == Command.STOP) {
				m_Model.stopServer();
			} else if(command == Command.SET) {
				/* TODO: Neaten this */
				if(commandArgs.length != 2) {
					throw new CommandException(cmdLine, "Expected 2 arguments");
				}
				
				/* TODO: Proper error checking */
				String property = commandArgs[0].toLowerCase();
				if(property.equals("port")) {
					m_Model.setWebDAVPort(Integer.parseInt(commandArgs[1]));
				}
				
			}
		} catch(Exception e) {
			throw new CommandException(cmdLine, e.getMessage());
		}
	}

	@Override
	public String getCommand() {
		return "service";
	}

	@Override
	public String getUsageString() {
		return "<name> <start|stop|set> [arg [arg [arg...]]]";
	}

	@Override
	public String getDescription() {
		return "Manage a service";
	}

	private Command parseCommand(String cmd) {
		switch(cmd.toLowerCase()) {
			case "start":
				return Command.START;
			case "stop":
				return Command.STOP;
			case "set":
				return Command.SET;
		}

		return null;
	}
}
