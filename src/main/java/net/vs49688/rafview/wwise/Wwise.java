package net.vs49688.rafview.wwise;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class Wwise {
	
	private static final int SECTION_BKHD = makeLEIntFromBytes('B', 'K', 'H', 'D');
	private static final int SECTION_DIDX = makeLEIntFromBytes('D', 'I', 'D', 'X');
	private static final int SECTION_DATA = makeLEIntFromBytes('D', 'A', 'T', 'A');
	private static final int SECTION_ENVS = makeLEIntFromBytes('E', 'N', 'V', 'S');
	private static final int SECTION_FXPR = makeLEIntFromBytes('F', 'X', 'P', 'R');
	private static final int SECTION_HIRC = makeLEIntFromBytes('H', 'I', 'R', 'C');
	private static final int SECTION_STID = makeLEIntFromBytes('S', 'T', 'I', 'D');
	private static final int SECTION_STMG = makeLEIntFromBytes('B', 'T', 'M', 'G');

	private static int makeLEIntFromBytes(char a, char b, char c, char d) {
		return ((d & 0xFF) << 24) | ((c & 0xFF) << 16) | ((b & 0xFF) << 8) | (a & 0xFF);
	}
	
	private static String sectionTypeToString(int section) {
		ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(section);
		return new String(bb.array());
	}
	
	public static void main(String[] args) throws Exception {
		File f = new File("C:\\Users\\Zane\\Desktop\\Wwise\\SFX\\Characters\\Vayne\\Skins\\Base\\Vayne_Base_SFX_audio.bnk");
		try(FileInputStream fis = new FileInputStream(f)) {
			MappedByteBuffer buffer;
			
			try(FileChannel fChannel = fis.getChannel()) {
				buffer = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size());
			}
			
			buffer.order(ByteOrder.LITTLE_ENDIAN);

			BankHeader header = null;
			DataIndex dindex = null;
			
			for(;;) {
				int section = buffer.getInt();
				System.err.printf("Found %s section\n", sectionTypeToString(section));
				
				long bytes = buffer.getInt() & 0xFFFFFFFFL;

				long nextBlock = buffer.position() + bytes;
				
				if(section == SECTION_BKHD) {
					if(header != null)
						throw new IOException("Multiple BKHD sections.");
					header = new BankHeader(section, bytes, buffer);
				} else if(section == SECTION_DIDX) {
					if(dindex != null)
						throw new IOException("Multiple DIDX sections.");
					dindex = new DataIndex(section, bytes, buffer);
				} else if(section == SECTION_DATA) {

				} else if(section == SECTION_ENVS) {

				} else if(section == SECTION_FXPR) {

				} else if(section == SECTION_HIRC) {

				} else if(section == SECTION_STID) {

				} else if(section == SECTION_STMG) {

				} else {
					throw new IOException("Unknown section encountered");
				}
				
				buffer.position((int)nextBlock);
			}
		}
	}
}
