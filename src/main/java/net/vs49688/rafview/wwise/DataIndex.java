package net.vs49688.rafview.wwise;

import java.nio.*;
import java.util.*;

public class DataIndex extends Section {
	
	private List<WEMEntry> m_WEMs;
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
