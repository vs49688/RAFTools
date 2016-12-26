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
package net.vs49688.rafview;

import java.util.*;
import net.vs49688.rafview.gui.*;
import net.vs49688.rafview.cli.*;
import net.vs49688.rafview.interpreter.Interpreter;

public class RAFView {
	
	private static void printUsage() {
		System.err.printf("Usage:\n");
		System.err.printf("RAFTools.jar [-console]\n");
		System.exit(1);
	}
	
	private static void startCLI() {
		Model model = new Model();
		CommandInterface cli = new CommandInterface(System.out, model);
		
		cli.start();
		
		try(Scanner stdin = new Scanner(System.in)) {
			System.out.printf("%s> ", model.getCurrentDirectory());
			while(stdin.hasNextLine()) {
				Interpreter.CommandResult res = cli.parseString(stdin.nextLine());
				
				while(res.getState() != Interpreter.CommandResult.State.COMPLETE) {}
				
				System.out.printf("%s> ", model.getCurrentDirectory());
			}
		}
		
		cli.stop();
	}
	
	public static void main(String[] args) throws Exception {
		
		if(args.length > 1) {
			printUsage();
			return;
		}
		
		if(args.length == 1 && args[0].equalsIgnoreCase("-console")) {
			startCLI();
		} else {
			Controller c = new Controller();
		}
	}
}
