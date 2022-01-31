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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.cert.Certificate;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.AbstractResource;
import org.apache.catalina.webresources.FileResource;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class NIOResource extends AbstractResource {

	protected static final Log logger = LogFactory.getLog(FileResource.class);
	protected Path path;

	private static String getWebappRoot(Path p) {
		String r = p.toString();
		if(Files.isDirectory(p) && p.getNameCount() != 0) {
			r += "/";
		}
		
		return r;
	}
	
	public NIOResource(WebResourceRoot root, Path path) {
		super(root, getWebappRoot(path));
		this.path = path;
	}

	@Override
	protected InputStream doGetInputStream() {
		try {
			return Files.newInputStream(path);
		} catch(IOException e) {
			if(logger.isErrorEnabled()) {
				logger.error(String.format("Files.newInputStream failed for %s", path.toString()), e);
			}
			
			return null;
		}
	}

	@Override
	protected Log getLog() {
		return logger;
	}

	@Override
	public long getLastModified() {
		try {
			return Files.getLastModifiedTime(path).to(TimeUnit.SECONDS);
		} catch(IOException e) {
			if(logger.isErrorEnabled()) {
				logger.error(String.format("Files.getLastModifiedTime failed for %s", path.toString()), e);
			}
			return 0;
		}
	}

	@Override
	public boolean exists() {
		return Files.exists(path);
	}

	@Override
	public boolean isVirtual() {
		return false;
	}

	@Override
	public boolean isDirectory() {
		return Files.isDirectory(path);
	}

	@Override
	public boolean isFile() {
		return !isDirectory();
	}

	@Override
	public boolean delete() {
		try {
			Files.delete(path);
			return true;
		} catch(IOException e) {
			if(logger.isErrorEnabled()) {
				logger.error(String.format("Files.delete failed for %s", path.toString()), e);
			}
			return false;
		}
	}

	@Override
	public String getName() {
		Path name = path.getFileName();
		return name == null ? "" : name.toString();
	}

	@Override
	public long getContentLength() {
		try {
			return Files.size(path);
		} catch(IOException e) {
			if(logger.isErrorEnabled()) {
				logger.error(String.format("Files.size failed for %s", path.toString()), e);
			}
			return 0L;
		}
	}

	@Override
	public String getCanonicalPath() {
		/* Always make this return null, the servlet will try
		 * to use sendfile if it's not, which won't work. */
		return null;
		//return m_Path.toAbsolutePath().toString();
	}

	@Override
	public boolean canRead() {
		return Files.isReadable(path);
	}

	@Override
	public byte[] getContent() {
		try {
			return Files.readAllBytes(path);
		} catch(IOException e) {
			if(logger.isErrorEnabled()) {
				logger.error(String.format("Files.readAllBytes failed for %s", path.toString()), e);
			}
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getCreation() {
		try {
			BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
			return attrs.creationTime().toMillis();
		} catch(IOException e) {
			if(logger.isErrorEnabled()) {
				logger.error(String.format("Files.readAttributes failed for %s", path.toString()), e);
			}
			return 0;
		}
	}

	@Override
	public URL getURL() {
		try {
			return path.toUri().toURL();
		} catch(MalformedURLException e) {
			return null;
		}
	}

	@Override
	public URL getCodeBase() {
		return getURL();
	}

	@Override
	public Certificate[] getCertificates() {
		return null;
	}

	@Override
	public Manifest getManifest() {
		return null;
	}

}
