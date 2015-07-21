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
package net.vs49688.rafview.sources;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.zip.*;

/**
 * Provides a way to get a synchronised DataSource from a file.
 */
public class SynchronisedFile {
	private final MappedByteBuffer m_ByteBuffer;
	private final long m_Size;
	private final Object m_Monitor;
	private final Path m_Path;
	
	/**
	 * Open a file.
	 * @param path The path to the file.
	 * @throws IOException If an I/O error occurred.
	 * @throws IllegalArgumentException If path == null.
	 */
	public SynchronisedFile(Path path) throws IOException, IllegalArgumentException {
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
	public final long getSize() {
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
	public final DataSource createDataSource(int offset, int size) throws IllegalArgumentException {
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
		
		byte[] compressed = new byte[size];
		
		synchronized(m_Monitor) {
			m_ByteBuffer.position(offset);
			m_ByteBuffer.get(compressed, 0, size);
		}
		
		return decompress(compressed, size);
	}

	protected byte[] decompress(byte[] data, int size) throws IOException {
		return data;
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
			
			if((offset + size) > SynchronisedFile.this.m_Size)
				throw new IllegalArgumentException("Offset + size out of bounds");

			m_Offset = offset;
			m_Size = size;
		}

		@Override
		public byte[] read() throws IOException {
			return safeRead(m_Offset, m_Size);
		}
		
		@Override
		public void close() {}
		
	}
}
