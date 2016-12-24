package net.vs49688.rafview.sources;

import java.io.IOException;
import java.util.Arrays;

public class CachedSource implements DataSource {

	private final byte[] m_Data;
	
	public CachedSource(byte[] data) {
		m_Data = Arrays.copyOf(data, data.length);
	}
	
	@Override
	public byte[] read() throws IOException {
		return Arrays.copyOf(m_Data, m_Data.length);
	}

	@Override
	public void close() {
		
	}
	
}
