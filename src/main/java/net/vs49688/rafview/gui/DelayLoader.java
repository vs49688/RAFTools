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
package net.vs49688.rafview.gui;

import java.io.*;
import java.nio.file.*;
import net.vs49688.rafview.sources.*;

public abstract class DelayLoader {
	
	protected abstract void load(Path path, byte[] data) throws Exception;
	protected abstract void onException(Exception e);
	
	public void delayLoad(Path path, DataSource ds) {
		new Thread(() -> {
			try {
				load(path, ds.read());
			} catch(Exception e) {
				onException(e);
			}
		}).start();
	}
	
	public void delayLoad(Path path) {
		if(path == null)
			return;
		
		new Thread(() -> {
			try {
				load(path.getFileName(), Files.readAllBytes(path));
			} catch(Exception e) {
				onException(e);
			}
		}).start();
	}
}
