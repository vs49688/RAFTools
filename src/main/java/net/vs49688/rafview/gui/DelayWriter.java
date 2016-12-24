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

import java.awt.Graphics;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import javax.imageio.*;
import net.vs49688.rafview.sources.*;

public abstract class DelayWriter {
	
	//protected abstract void write(Path f, byte[] data) throws Exception;
	protected abstract void onException(Exception e);
	
	public void delayWriteImage(BufferedImage image, String formatName, File output) {
		/* This is going to be used by the DDS viewer, so we'll need to copy the image.
		 * It's better in the long run anyway */
		BufferedImage backup = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics g = backup.getGraphics();
		g.drawImage(backup, 0, 0, null);
		
		new Thread(() -> {
			try {
				if(!ImageIO.write(image, formatName, output/*bos*/))
					throw new Exception(String.format("No appropriate writer for %s was found", formatName));

			} catch(Exception e) {
				onException(e);
			}
		}).start();
	}
	
	public void delayWrite(Path path, DataSource ds) {
		new Thread(() -> {
			try {
				Files.write(path, ds.read());
			} catch(Exception e) {
				onException(e);
			}
		}).start();
	}
}
