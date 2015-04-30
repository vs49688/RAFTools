package net.vs49688.rafview.cli.commands;

import java.nio.file.Paths;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.interpreter.*;

public class Extract implements ICommand {

	private final Model m_Model;
	private final Appendable m_Console;
	
	public Extract(Appendable out, Model model) {
		m_Console = out;
		m_Model = model;
	}
	
	@Override
	public void process(String cmdLine, String[] args) throws CommandException, Exception {
		if(args.length < 3)
			throw new CommandException(cmdLine, getUsageString());
		
		String outDir = args[args.length-1];
		
		//for(int i = 1; i < args.length-1; ++i) {
		//	m_Model.getVFS().extract(Paths.get(args[i]), Paths.get(outDir));
		//}
	}

	@Override
	public String getCommand() {
		return "extract";
	}

	@Override
	public String getUsageString() {
		return "file1... [file2... [...]] output_directory";
	}
	
	@Override
	public String getDescription() {
		return "Extract a file or directory";
	}
}
