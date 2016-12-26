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
package net.vs49688.rafview.wwise;

import java.nio.*;
import java.util.*;

public class DataIndex extends Section {
	
	private final List<WEMEntry> m_WEMs;
	
	public static class WEMEntry {
		private WEMEntry(long id, long offset, long length) {
			this.id = id;
			this.offset = offset;
			this.length = length;
		}
		public final long id;
		public final long offset;
		public final long length;
	}

	public DataIndex(int section, long length, ByteBuffer data) throws WwiseFormatException {
		super(section, length);
		
		int numFiles = (int)(length / 12);
		if((length % 12) != 0)
			throw new WwiseFormatException(String.format("DIDX: Invalid section length. Must be divisible by 12.", data.capacity()));

		m_WEMs = new ArrayList<>(numFiles);
		
		for(int i = 0; i < numFiles; ++i) {
			long id = data.getInt() & 0xFFFFFFFFL;
			long offset = data.getInt() & 0xFFFFFFFFL;
			long len = data.getInt() & 0xFFFFFFFFL;
			
			m_WEMs.add(new WEMEntry(id, offset, len));
		}
	}
	
	public List<WEMEntry> getWEMList() {
		return Collections.unmodifiableList(m_WEMs);
	}
}
