/*
 * RAFTools - Copyright (C) 2015 Zane van Iperen.
 *    Contact: zane.vaniperen@uqconnect.edu.au
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

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import net.vs49688.rafview.sources.*;

// http://wiki.xentax.com/index.php?title=Wwise_SoundBank_(*.bnk)

public class Wwise {
	
	private static final int SECTION_BKHD = makeLEIntFromBytes('B', 'K', 'H', 'D');
	private static final int SECTION_DIDX = makeLEIntFromBytes('D', 'I', 'D', 'X');
	private static final int SECTION_DATA = makeLEIntFromBytes('D', 'A', 'T', 'A');
	private static final int SECTION_ENVS = makeLEIntFromBytes('E', 'N', 'V', 'S');
	private static final int SECTION_FXPR = makeLEIntFromBytes('F', 'X', 'P', 'R');
	private static final int SECTION_HIRC = makeLEIntFromBytes('H', 'I', 'R', 'C');
	private static final int SECTION_STID = makeLEIntFromBytes('S', 'T', 'I', 'D');
	private static final int SECTION_STMG = makeLEIntFromBytes('B', 'T', 'M', 'G');

	private final Map<Long, DataSource> m_WEMFiles;
	
	private Wwise() {
		m_WEMFiles = new HashMap<>();
	}
	
	public static Wwise load(MappedByteBuffer buffer) throws WwiseFormatException {
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		Wwise wwise = new Wwise();
		Map<Integer, Section> sectionMap = new HashMap<>();

		/* Read all the sections in the file */
		for(; buffer.hasRemaining();) {
			int section = buffer.getInt();
			System.err.printf("Found %s section\n", sectionTypeToString(section));

			if(sectionMap.containsKey(section))
				throw new WwiseFormatException(String.format("Multiple %s sections found (offset %d).", sectionTypeToString(section), buffer.limit()));

			long bytes = buffer.getInt() & 0xFFFFFFFFL;

			long nextBlock = buffer.position() + bytes;

			if(section == SECTION_BKHD) {
				sectionMap.put(SECTION_BKHD, new BankHeader(section, bytes, buffer));
			} else if(section == SECTION_DIDX) {
				sectionMap.put(SECTION_DIDX, new DataIndex(section, bytes, buffer));
			} else if(section == SECTION_DATA) {
				sectionMap.put(SECTION_DATA, new Data(section, bytes, buffer));
			} else if(section == SECTION_ENVS) {
				throw new UnsupportedOperationException("ENVS section found, but not implemented. Please send this file to the developer.");
			} else if(section == SECTION_FXPR) {
				throw new UnsupportedOperationException("FXPR section found, but not implemented. Please send this file to the developer.");
			} else if(section == SECTION_HIRC) {
				throw new UnsupportedOperationException("HIRC section found, but not implemented. Please send this file to the developer.");
			} else if(section == SECTION_STID) {
				throw new UnsupportedOperationException("STID section found, but not implemented. Please send this file to the developer.");
			} else if(section == SECTION_STMG) {
				throw new UnsupportedOperationException("STMG section found, but not implemented. Please send this file to the developer.");
			} else {
				throw new WwiseFormatException("Unknown section encountered");
			}

			/* How I wished ByteBuffer used longs */
			buffer.position((int)nextBlock);
		}

		/* Now that all the sections have been read, start reading the content */

		SynchronisedFile bnk = new SynchronisedFile(buffer);

		wwise.loadWEM(bnk, sectionMap);
		return wwise;
	}
	
	public static void main(String[] args) throws Exception {
		File f = new File("F:\\Wwise\\SFX\\Characters\\Vayne\\Skins\\Base\\Vayne_Base_SFX_audio.bnk");
		
		Wwise wwise = null;
		
		try(FileInputStream fis = new FileInputStream(f)) {
			MappedByteBuffer buffer;
			
			try(FileChannel fChannel = fis.getChannel()) {
				buffer = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size());
			}

			wwise = load(buffer);
		}
		
		int x = 0;
	}
	
	private void loadWEM(SynchronisedFile bnk, Map<Integer, Section> sections) throws WwiseFormatException {
		
		DataIndex didx = (DataIndex)sections.getOrDefault(SECTION_DIDX, null);
		Data data = (Data)sections.getOrDefault(SECTION_DATA, null);
		
		/* If they both don't exist, we don't have any embedded WEMs */
		if(didx == null && data == null) {
			return;
		/* If we have no DataIndex, but have a Data section, then it's a bad file. */
		} else if(didx == null && data != null) {
			throw new WwiseFormatException("Found DATA section with no DIDX.");
		/* If we have a DataIndex, but no Data section, then it's a bad file */
		} else if(didx != null & data == null) {
			throw new WwiseFormatException("Found DIDX section with no DATA.");
		}

		List<DataIndex.WEMEntry> idx = didx.getWEMList();
		
		for(final DataIndex.WEMEntry entry : idx) {
			m_WEMFiles.put(entry.id, bnk.createDataSource(data.getOffset() + (int)entry.offset, (int)entry.length));
		}
		
//		for(final Long l : wem.keySet()) {
//			DataSource ds = wem.get(l);
//			
//			Files.write(Paths.get("C:", "wem", String.format("%d.wem", l)), ds.read());
//		}

	}
	
	private static int makeLEIntFromBytes(char a, char b, char c, char d) {
		return ((d & 0xFF) << 24) | ((c & 0xFF) << 16) | ((b & 0xFF) << 8) | (a & 0xFF);
	}
	
	private static String sectionTypeToString(int section) {
		ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(section);
		return new String(bb.array());
	}
}
