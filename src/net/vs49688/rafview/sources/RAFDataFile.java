package net.vs49688.rafview.sources;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.zip.*;

/**
 * A representation of a RAF data file.
 */
public class RAFDataFile {
	private final MappedByteBuffer m_ByteBuffer;
	private final long m_Size;
	private final Object m_Monitor;
	private final Path m_Path;
	
	/**
	 * Open a RAF data file (.raf.dat)
	 * @param path The path to the file.
	 * @throws IOException If an I/O error occurred.
	 * @throws IllegalArgumentException If path == null.
	 */
	public RAFDataFile(Path path) throws IOException, IllegalArgumentException {
		if(path == null)
			throw new IllegalArgumentException("Path cannot be null");
		
		try (FileChannel fc = new FileInputStream(path.toFile()).getChannel()) {
			m_ByteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			m_Size = fc.size();
		}
		
		m_Path = path;
		m_Monitor = new Object();
	}
	
	/**
	 * Get the size of the file in bytes.
	 * @return The size of the file in bytes.
	 */
	public long getSize() {
		return m_Size;
	}
	
	/**
	 * Create a DataSource using the specified offset and size.
	 * 
	 * (offset + size) must not be greater than getSize()
	 * 
	 * @param offset The offset within the file.
	 * @param size The size of the segment.
	 * @return A DataSource using the specified offset and size.
	 * @throws IllegalArgumentException 
	 */
	public DataSource createDataSource(int offset, int size) throws IllegalArgumentException {
		return new ChunkSource(offset, size);
	}
	
	/**
	 * Read a block of data, decompressing it on the way.
	 * @param offset The offset into the RAF archive.
	 * @param size The size of the block.
	 * @return The read data.
	 * @throws IOException If an I/O error occurred.
	 */
	private byte[] safeRead(int offset, int size) throws IOException {
		
		byte[] compressed = new byte[size], tmp = new byte[1024];
		Inflater inf = new Inflater();
		
		synchronized(m_Monitor) {
			m_ByteBuffer.position(offset);
			m_ByteBuffer.get(compressed, 0, size);
		}

		inf.setInput(compressed);
		
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream(size)) {	
			while(!inf.finished())
				bos.write(tmp, 0, inf.inflate(tmp));

			tmp = bos.toByteArray();
		} catch(DataFormatException e) {
			/* We're uncompressed, just return the raw data */
			return compressed;
		} catch(IOException e) {
			throw e;
		}
		
		inf.end();
		return tmp;
	}
	
	private class ChunkSource implements DataSource {

		private final int m_Offset;
		private final int m_Size;
		
		/**
		 * Create a new ChunkSource
		 * @param offset The offset into the file.
		 * @param size The size of the chunk in bytes.
		 */
		public ChunkSource(int offset, int size) throws IllegalArgumentException {
			if(offset < 0)
				throw new IllegalArgumentException("Cannot have a negative offset");
			
			if(size < 0)
				throw new IllegalArgumentException("Cannot have a negative size");
			
			if((offset + size) > RAFDataFile.this.m_Size)
				throw new IllegalArgumentException("Offset + size out of bounds");

			m_Offset = offset;
			m_Size = size;
		}
		
		@Override
		public long size() {
			/* They're compressed, so we don't know the size */
			return -1;
		}

		@Override
		public byte[] read() throws IOException {
			return safeRead(m_Offset, m_Size);
		}
		
	}
}
