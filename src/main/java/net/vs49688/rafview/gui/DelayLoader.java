package net.vs49688.rafview.gui;

import java.io.*;
import java.nio.file.*;
import net.vs49688.rafview.sources.*;

public abstract class DelayLoader {
	
	protected abstract void load(String name, byte[] data) throws Exception;
	protected abstract void onException(Exception e);
	
	public void delayLoad(String name, DataSource ds) {
		new Thread(() -> {
			try {
				load(name, ds.read());
			} catch(Exception e) {
				onException(e);
			}
		}).start();
	}
	
	public void delayLoad(File f) {
		if(f == null)
			return;
		
		new Thread(() -> {
			try {
				load(f.getName(), Files.readAllBytes(f.toPath()));
			} catch(Exception e) {
				onException(e);
			}
		}).start();
	}
}
