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
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.TrackedWebResource;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.util.LifecycleBase;
import org.apache.catalina.webresources.EmptyResource;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NIOResourceRoot extends LifecycleBase implements WebResourceRoot {

	protected static final Logger logger = LogManager.getFormatterLogger(NIOResourceRoot.class);

	protected Context context;
	protected String webAppPath;
	protected Path rootPath;

	public NIOResourceRoot(String webAppPath, Path rootPath) {
		context = null;
		this.webAppPath = webAppPath;
		this.rootPath = rootPath;
	}

	@Override
	public WebResource getResource(String path) {
		return createResource(rootPath.resolve(path));
	}

	@Override
	public WebResource[] getResources(String path) {
		Path _path = rootPath.resolve(rootPath);
		
		if(!Files.exists(_path)) {
			return new WebResource[0];
		}
		
		if(!Files.isDirectory(_path)) {
			return new WebResource[] {
				createResource(_path)
			};
		}
		
		return listResources(path);
	}

	@Override
	public WebResource getClassLoaderResource(String path) {
		return new EmptyResource(this, webAppPath);
	}

	@Override
	public WebResource[] getClassLoaderResources(String path) {
		return new WebResource[0];
	}

	@Override
	public String[] list(String path) {
		// NB: Only return the name, no trailing / if a directory.

		Path p = rootPath.resolve(path);
		
		if(!Files.isDirectory(p)) {
			return new String[0];
		}

		Path[] children = listDirectory(p);
		String[] s = new String[children.length];

		for(int i = 0; i < children.length; ++i) {
			s[i] = children[i].getFileName().toString();
		}

		return s;
	}

	@Override
	public Set<String> listWebAppPaths(String path) {
		return new HashSet<>();
	}

	private Path[] listDirectory(Path path) {
		if(path == null) {
			return new Path[0];
		}

		List<Path> dirs = new ArrayList<>();
		try {
			Files.list(path).forEach((Path p) -> {
				dirs.add(p);
			});
		} catch(IOException e) {
			if(logger.isErrorEnabled()) {
				logger.error(String.format("Files.list failed for %s", path.toString()), e);
			}
			return new Path[0];
		}

		return dirs.toArray(new Path[dirs.size()]);
	}

	@Override
	public WebResource[] listResources(String path) {
		Path[] children = listDirectory(rootPath.resolve(path));
		WebResource[] res = new WebResource[children.length];

		for(int i = 0; i < children.length; ++i) {
			res[i] = createResource(children[i]);
		}

		return res;
	}

	@Override
	public boolean mkdir(String path) {
		Path rawPath = rootPath.resolve(path);

		Path parent = rawPath.getParent();
		if(parent == null) {
			/* Is root */
			return false;
		}

		/* Subfolder of a file? No. */
		if(!Files.isDirectory(parent)) {
			return false;
		}

		/* If we already exist */
		if(Files.exists(rawPath)) {
			/* And we're not a directory, then no. */
			if(!Files.isDirectory(rawPath)) {
				return false;
			}
		} else {
			try {
				Files.createDirectory(rawPath);
			} catch(IOException e) {
				if(logger.isErrorEnabled()) {
					logger.error(String.format("Files.createDirectory failed for %s", path.toString()), e);
				}
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean write(String path, InputStream is, boolean overwrite) {
		Path p = rootPath.resolve(path);
		if(!overwrite && Files.exists(p)) {
			return false;
		}

		if(Files.isDirectory(p)) {
			return false;
		}

		try(OutputStream os = Files.newOutputStream(p)) {
			byte[] buffer = new byte[1024];

			for(int amt = -1; (amt = is.read(buffer)) >= 0;) {
				os.write(buffer, 0, amt);
			}
		} catch(IOException e) {
			if(logger.isErrorEnabled()) {
				logger.error(String.format("OutputStream#write failed for %s", path.toString()), e);
			}
			return false;
		}

		return true;
	}

	@Override
	public void createWebResourceSet(ResourceSetType type, String webAppMount, URL url, String internalPath) {

	}

	@Override
	public void createWebResourceSet(ResourceSetType type, String webAppMount, String base, String archivePath, String internalPath) {

	}

	@Override
	public void addPreResources(WebResourceSet webResourceSet) {

	}

	@Override
	public WebResourceSet[] getPreResources() {
		return new WebResourceSet[0];
	}

	@Override
	public void addJarResources(WebResourceSet webResourceSet) {

	}

	@Override
	public WebResourceSet[] getJarResources() {
		return new WebResourceSet[0];
	}

	@Override
	public void addPostResources(WebResourceSet webResourceSet) {

	}

	@Override
	public WebResourceSet[] getPostResources() {
		return new WebResourceSet[0];

	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public void setAllowLinking(boolean allowLinking) {

	}

	@Override
	public boolean getAllowLinking() {
		return false;
	}

	@Override
	public void setCachingAllowed(boolean cachingAllowed) {

	}

	@Override
	public boolean isCachingAllowed() {
		return false;
	}

	@Override
	public void setCacheTtl(long ttl) {

	}

	@Override
	public long getCacheTtl() {
		return Long.MAX_VALUE;
	}

	@Override
	public void setCacheMaxSize(long cacheMaxSize) {

	}

	@Override
	public long getCacheMaxSize() {
		return Long.MAX_VALUE;
	}

	@Override
	public void setCacheObjectMaxSize(int cacheObjectMaxSize) {

	}

	@Override
	public int getCacheObjectMaxSize() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void setTrackLockedFiles(boolean trackLockedFiles) {

	}

	@Override
	public boolean getTrackLockedFiles() {
		return false;
	}

	@Override
	public void backgroundProcess() {
		
	}

	@Override
	public void registerTrackedResource(TrackedWebResource trackedResource) {

	}

	@Override
	public void deregisterTrackedResource(TrackedWebResource trackedResource) {
		
	}

	@Override
	public List<URL> getBaseUrls() {
		return new ArrayList<>();
	}

	@Override
	public void gc() {
		System.gc();
	}

	@Override
	public LifecycleListener[] findLifecycleListeners() {
		return new LifecycleListener[0];
	}

	@Override
	protected void initInternal() throws LifecycleException {
		TomcatURLStreamHandlerFactory.register();
	}

	@Override
	protected void startInternal() throws LifecycleException {
		setState(LifecycleState.STARTING);
	}

	@Override
	protected void stopInternal() throws LifecycleException {
		setState(LifecycleState.STOPPING);
	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		
	}

	protected NIOResource createResource(Path path) {
		return new NIOResource(this, path);
	}
}
