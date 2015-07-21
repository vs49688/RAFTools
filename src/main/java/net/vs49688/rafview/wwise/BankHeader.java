package net.vs49688.rafview.wwise;

import java.nio.*;

public class BankHeader extends Section {

	private final long m_Version;
	private final long m_ID;
	
	public BankHeader(int section, long length, ByteBuffer data) throws WwiseFormatException {
		super(section, length);
		
		if(length != 24)
			throw new WwiseFormatException(String.format("BKHD: Invalid section length. Expected 24, got %d.", length));

		m_Version = (long)data.getInt() & 0xFFFFFFFFL;
		m_ID = (long)data.getInt() & 0xFFFFFFFFL;
		
		if(data.getInt() != 0 || data.getInt() != 0) {
			throw new WwiseFormatException("BKHD: Reserved fields not zero.");
		}
	}
	
	public long getVersion() {
		return m_Version;
	}
	
	public long getID() {
		return m_ID;
	}
}
