package net.vs49688.rafview.cli.webdav;

import java.io.IOException;
import java.io.InputStream;
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

	private static final Log m_Log = LogFactory.getLog(FileResource.class);
	private Path m_Path;

	public NIOResource(WebResourceRoot root, String webAppPath, Path path) {
		super(root, webAppPath);
		m_Path = path;
	}

	@Override
	protected InputStream doGetInputStream() {
		try {
			return Files.newInputStream(m_Path);
		} catch(IOException e) {
			logIOException(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Log getLog() {
		return m_Log;
	}

	@Override
	public long getLastModified() {
		try {
			return Files.getLastModifiedTime(m_Path).to(TimeUnit.SECONDS);
		} catch(IOException e) {
			logIOException(e);
			return 0;
		}
	}

	private void logIOException(IOException e) {
		if(m_Log.isDebugEnabled()) {
			m_Log.debug("", e);
		}
	}

	@Override
	public boolean exists() {
		return Files.exists(m_Path);
	}

	@Override
	public boolean isVirtual() {
		return false;
	}

	@Override
	public boolean isDirectory() {
		return Files.isDirectory(m_Path);
	}

	@Override
	public boolean isFile() {
		return !isDirectory();
	}

	@Override
	public boolean delete() {
		try {
			Files.delete(m_Path);
			return true;
		} catch(IOException e) {
			logIOException(e);
			return false;
		}
	}

	@Override
	public String getName() {
		return m_Path.getName(m_Path.getNameCount() - 1).toString();
	}

	@Override
	public long getContentLength() {
		try {
			return Files.size(m_Path);
		} catch(IOException e) {
			logIOException(e);
			return 0L;
		}
	}

	@Override
	public String getCanonicalPath() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean canRead() {
		return Files.isReadable(m_Path);
	}

	@Override
	public byte[] getContent() {
		try {
			return Files.readAllBytes(m_Path);
		} catch(IOException e) {
			logIOException(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getCreation() {
		try {
			BasicFileAttributes attrs = Files.readAttributes(m_Path, BasicFileAttributes.class);
			return attrs.creationTime().toMillis();
		} catch(IOException e) {
			logIOException(e);
			return 0;
		}
	}

	@Override
	public URL getURL() {
		throw new UnsupportedOperationException("Not supported yet.");
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
