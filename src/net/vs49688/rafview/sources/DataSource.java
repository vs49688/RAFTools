package net.vs49688.rafview.sources;

import java.io.Closeable;
import java.io.IOException;

/**
 * The interface for a data source.
 * This may be anything, a RAF archive, a ZIP file, whatever tickles your fancy.
 */
public interface DataSource {
	/**
	 * Get the size of the data in bytes. This may return -1 if the souce
	 * is assigned to a stream and the size cannot be determined.
	 * @return The size of the data in bytes.
	 */
	public long size();
	
	/**
	 * Read the data.
	 * @return The read data.
	 * @throws IOException If an I/O error occurred.
	 */
	public byte[] read() throws IOException;
	
	public void close();
}
