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
package net.vs49688.rafview.sources;

import java.io.*;
import java.nio.file.Files;

/**
 * The interface for a data source.
 * This may be anything, a RAF archive, a ZIP file, whatever tickles your fancy.
 */
public interface DataSource {
//	/**
//	 * Get the size of the data in bytes. This may return -1 if the souce
//	 * is assigned to a stream and the size cannot be determined.
//	 * @return The size of the data in bytes.
//	 */
//	public long size();
	
	/**
	 * Read the data.
	 * @return The read data.
	 * @throws IOException If an I/O error occurred.
	 */
	public byte[] read() throws IOException;
	
	public void close();
	
	public static void dumpToFile(DataSource ds, java.nio.file.Path path) throws IOException {
		Files.write(path, ds.read());
	}
}
