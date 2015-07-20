package net.vs49688.rafview;

import java.util.*;
import net.vs49688.rafview.gui.*;
import net.vs49688.rafview.cli.*;
import net.vs49688.rafview.interpreter.Interpreter;

// http://wiki.xentax.com/index.php?title=Wwise_SoundBank_(*.bnk)

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
			System.out.print("> ");
			while(stdin.hasNextLine()) {
				Interpreter.CommandResult res = cli.parseString(stdin.nextLine());
				
				while(res.getState() != Interpreter.CommandResult.State.COMPLETE) {}
				
				System.out.print("> ");
			}
		}
		
		cli.stop();
	}
	
	public static void main(String[] args) {
		
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
