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
package net.vs49688.rafview.sources;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;
import java.util.zip.*;

public class PossiblyZippedSynchronisedFile extends SynchronisedFile {

	public PossiblyZippedSynchronisedFile(Path path) throws IOException, IllegalArgumentException {
		super(path);
	}

	public PossiblyZippedSynchronisedFile(MappedByteBuffer buffer) throws IllegalArgumentException {
		super(buffer);
	}
	
	@Override
	protected byte[] decompress(byte[] data, int size) throws IOException {
		byte[] tmp = new byte[1024];
		Inflater inf = new Inflater();

		inf.setInput(data);
		
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream(size)) {	
			while(!inf.finished())
				bos.write(tmp, 0, inf.inflate(tmp));

			tmp = bos.toByteArray();
		} catch(DataFormatException e) {
			/* We're uncompressed, just return the raw data */
			return data;
		} catch(IOException e) {
			throw e;
		}
		
		inf.end();
		return tmp;
	}
}
