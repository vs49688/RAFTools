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
package net.vs49688.rafview.interpreter;

import java.util.*;
import java.util.concurrent.*;
import java.text.ParseException;

public class Interpreter {

	private final ConcurrentHashMap<String, ICommand> m_Handlers;
	private final ArrayBlockingQueue<UUID> m_CommandQueue;
	private final Map<UUID, CommandResult> m_ResultStorage;
	private final Thread m_Thread;
	private volatile boolean m_bWantExit;
	private volatile boolean m_bInCommand = true;
	private final UUID m_ExitUUID;
	private final IFuckedUp m_IFuckedUp;

	public Interpreter(IFuckedUp fuck) {
		m_Handlers = new ConcurrentHashMap<>();
		m_CommandQueue = new ArrayBlockingQueue<>(32);
		m_ResultStorage = (Map) Collections.synchronizedMap(new LinkedHashMap<>());
		m_bWantExit = false;
		m_Thread = new Thread(new CommandThread());

		m_ExitUUID = UUID.randomUUID();
		
		m_IFuckedUp = fuck;
	}

	public void start() {
		m_Thread.start();
	}

	public void stop() {
		m_bWantExit = true;
		if (m_Thread.isAlive()) {
			m_CommandQueue.add(m_ExitUUID);
		}
	}

	public boolean inCommand() {
		return m_bInCommand;
	}

	@SuppressWarnings("empty-statement")
	public CommandResult executeCommand(String command) {
		CommandResult res = new CommandResult(UUID.randomUUID(), command);
		m_ResultStorage.put(res.m_UUID, res);

		/* Add the command. If the queue is full, wait until it isn't */
		while (m_CommandQueue.offer(res.m_UUID) == false);

		return res;
	}

	public void registerCommand(ICommand handler) {
		if (handler == null) {
			throw new IllegalArgumentException();
		}
		if (m_Handlers.get(handler.getCommand()) == null) {
			m_Handlers.putIfAbsent(handler.getCommand(), handler);
		} else {
			m_Handlers.replace(handler.getCommand(), handler);
		}
	}

	public void unregisterCommand(String command) {
		if (command == null) {
			throw new IllegalArgumentException();
		}

		m_Handlers.remove(command);
	}

	public void unregisterCommand(ICommand handler) {
		if (handler == null) {
			throw new IllegalArgumentException();
		}

		unregisterCommand(handler.getCommand());
	}

	/**
	 * Crack a command line.
	 *
	 * @param toProcess the command line to process.
	 * @return the command line broken into strings. An empty or null toProcess
	 * parameter results in a zero sized array.
	 * @throws java.text.ParseException
	 *
	 * Ripped from ANT 1.9.4
	 */
	public static String[] translateCommandline(String toProcess) throws ParseException {
		if (toProcess == null || toProcess.length() == 0) {
			//no command? no string
			return new String[0];
		}
		// parse with a simple finite state machine

		final int normal = 0;
		final int inQuote = 1;
		final int inDoubleQuote = 2;
		int state = normal;
		final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
		final ArrayList<String> result = new ArrayList<>();
		final StringBuilder current = new StringBuilder();
		boolean lastTokenHasBeenQuoted = false;

		while (tok.hasMoreTokens()) {
			String nextTok = tok.nextToken();
			switch (state) {
				case inQuote:
					if ("\'".equals(nextTok)) {
						lastTokenHasBeenQuoted = true;
						state = normal;
					} else {
						current.append(nextTok);
					}
					break;
				case inDoubleQuote:
					if ("\"".equals(nextTok)) {
						lastTokenHasBeenQuoted = true;
						state = normal;
					} else {
						current.append(nextTok);
					}
					break;
				default:
					if (null != nextTok) {
						switch (nextTok) {
							case "\'":
								state = inQuote;
								break;
							case "\"":
								state = inDoubleQuote;
								break;
							case " ":
								if (lastTokenHasBeenQuoted || current.length() != 0) {
									result.add(current.toString());
									current.setLength(0);
								}
								break;
							default:
								current.append(nextTok);
								break;
						}
					}
					lastTokenHasBeenQuoted = false;
					break;
			}
		}
		if (lastTokenHasBeenQuoted || current.length() != 0) {
			result.add(current.toString());
		}

		if (state == inQuote || state == inDoubleQuote) {
			throw new ParseException("unbalanced quotes in " + toProcess, -1);
		}

		return result.toArray(new String[result.size()]);
	}

	private class CommandThread implements Runnable {

		@Override
		public void run() {
			for (; !m_bWantExit;) {
				UUID uuid;
				try {
					uuid = m_CommandQueue.take();
				} catch (InterruptedException e) {
					continue;
				}

				CommandResult res = m_ResultStorage.get(uuid);

				try {

					String command;
					if (uuid.equals(m_ExitUUID)) {
						command = "";
					} else {
						command = res.m_CommandLine;
					}

					String[] args;
					try {
						args = translateCommandline(command);
					} catch (ParseException e) {
						throw new CommandException(command, e.getMessage());
					}

					if (args.length == 0) {
						continue;
					}

					if (!m_Handlers.containsKey(args[0])) {
						throw new CommandException(command, "Unknown Command");
					}

					m_bInCommand = true;
					res.m_State = CommandResult.State.IN_PROGRESS;
					m_Handlers.get(args[0]).process(command, args);
				} catch (Exception e) {
					res.m_Exception = e;
					m_IFuckedUp.onFuckup(res);
				} finally {
					m_bInCommand = false;
					if (res != null) {
						res.m_State = CommandResult.State.COMPLETE;
					}
				}
			}

			m_bWantExit = false;
		}
	};

	public static final class CommandResult {

		public enum State {

			PENDING,
			IN_PROGRESS,
			COMPLETE
		}

		public CommandResult(UUID uuid, String cmdLine) {
			m_UUID = uuid;
			m_CommandLine = cmdLine;
			m_Exception = null;
			m_State = State.PENDING;
		}

		public final UUID getUUID() {
			return m_UUID;
		}

		public final String getCommand() {
			return m_CommandLine;
		}

		public final Exception getException() {
			return m_Exception;
		}

		public final State getState() {
			return m_State;
		}

		private final UUID m_UUID;
		private final String m_CommandLine;
		private volatile Exception m_Exception;
		private volatile State m_State;
	}

}
