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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import net.vs49688.rafview.vfs.RAFS;
import org.apache.catalina.WebResource;
import org.apache.catalina.webresources.EmptyResource;

public class RAFSResourceRoot extends NIOResourceRoot {

	private final RAFS rafs;

	public RAFSResourceRoot(String webAppPath, RAFS rafs) {
		super(webAppPath, rafs.getRoot());
		this.rafs = rafs;
	}

	@Override
	public WebResource getResource(String path) {
		logger.debug("Request for resource: %s", path);
		if(path.startsWith("/META-INF") || path.startsWith("/WEB-INF")) {
			logger.debug("  Forbidden path, returning empty");
			return new EmptyResource(this, path);
		}

		return super.getResource(path);
	}

	@Override
	public WebResource[] listResources(String path) {
		if(path.startsWith("/META-INF") || path.startsWith("/WEB-INF")) {
			logger.debug("  Forbidden path, returning empty");
			return new WebResource[0];
		}

		return super.listResources(path);
	}

	@Override
	protected NIOResource createResource(Path path) {
		return new RAFSResource(this, path, rafs);
	}

	@Override
	public boolean write(String path, InputStream is, boolean overwrite) {

		/* If we're a RAF file, remove the "internal" owner. */
		Path p = rootPath.resolve(path);
		if(Files.exists(p)) {
			try {
				PosixFileAttributeView a = Files.getFileAttributeView(p, PosixFileAttributeView.class);
				if(a.getOwner() == rafs.getOwnerPrincipal()) {
					a.setOwner(null);
				}
			} catch(IOException e) {
				if(logger.isErrorEnabled()) {
					logger.error(String.format("Error removing owner attribute on %s. Attempting deletion...", path), e);
				}
				try {
					/* If that failed, try to delete and recreate. */
					Files.delete(p);
					Files.createFile(p);
				} catch(IOException ex) {
					/* If that failed, just die. */
					if(logger.isErrorEnabled()) {
						logger.error("  Deletion failed, aborting write...", e);
					}
					return false;
				}
			}
		}

		return super.write(path, is, overwrite);
	}
}
