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
package net.vs49688.rafview.cli.webdav;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import net.vs49688.rafview.vfs.RAFS;
import net.vs49688.rafview.vfs.Version;
import org.apache.catalina.WebResourceRoot;

public class RAFSResource extends NIOResource {

	protected RAFS m_RAFS;

	private byte[] m_Cache;

	public RAFSResource(WebResourceRoot root, Path path, RAFS rafs) {
		super(root, path);

		m_RAFS = rafs;

		m_Cache = null;

		try {
			
			if(Files.exists(path) && !Files.isDirectory(path) && isInRAF()) {
				Version v = rafs.getVersionDataForFile(path, null);
				if(v != null) {
					m_Cache = v.dataSource.read();
				}
			}
		} catch(IOException e) {
			m_Cache = null;
		}
	}

	private void recheckCache() {
		try {
			if(m_Cache != null) {
				/* We might have been changed, check we're still sourcing the archive. */
				if(!isInRAF()) {
					m_Cache = null;
				}
			}
		} catch(IOException e) {
			m_Cache = null;
		}
	}

	@Override
	protected InputStream doGetInputStream() {
		recheckCache();
		return m_Cache != null ? new ByteArrayInputStream(m_Cache) : super.doGetInputStream();
	}

	@Override
	public long getContentLength() {
		recheckCache();
		return m_Cache != null ? m_Cache.length : super.getContentLength();
	}

	@Override
	public byte[] getContent() {
		recheckCache();
		return m_Cache != null ? m_Cache : super.getContent();
	}

	private boolean isInRAF() throws IOException {
		/* Always check if the RAF flag is set. We might have been deleted and re-created.
		 * so our (new) data should be stored in memory, but the old one will still be loaded internally. */
		PosixFileAttributeView a = Files.getFileAttributeView(path, PosixFileAttributeView.class);
		return a.getOwner() == m_RAFS.getOwnerPrincipal();
	}

	void unsetRAF() throws IOException {
		PosixFileAttributeView a = Files.getFileAttributeView(path, PosixFileAttributeView.class);
		if(a.getOwner() == m_RAFS.getOwnerPrincipal()) {
			a.setOwner(null);
		}
	}

}
