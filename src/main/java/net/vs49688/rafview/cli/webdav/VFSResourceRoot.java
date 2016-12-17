package net.vs49688.rafview.cli.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.vs49688.rafview.vfs.RAFS;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VFSResourceRoot extends LifecycleBase implements WebResourceRoot {

	private static final Logger logger = LogManager.getFormatterLogger(VFSResourceRoot.class);

	private Context m_Context;
	private String m_WebAppPath;
	private RAFS m_VFS;

	public VFSResourceRoot(String webAppPath, RAFS vfs) {
		m_Context = null;
		m_WebAppPath = webAppPath;
		m_VFS = vfs;
	}

	@Override
	public WebResource getResource(String path) {
		logger.debug("Request for resource: %s", path);
		if(path.startsWith("/META-INF") || path.startsWith("/WEB-INF")) {
			logger.debug("  Forbidden path, returning empty");
			return new EmptyResource(this, path);
		}

		return new NIOResource(this, m_WebAppPath, m_VFS.getFileSystem().getPath(path));
	}

	@Override
	public WebResource[] getResources(String path) {
		return new WebResource[0];
	}

	@Override
	public WebResource getClassLoaderResource(String path) {
		return new EmptyResource(this, path);
	}

	@Override
	public WebResource[] getClassLoaderResources(String path) {
		return new WebResource[0];
	}

	@Override
	public String[] list(String path) {
		Path p = m_VFS.getFileSystem().getPath(path);
		
		Path[] children = listDirectory(p);
		String[] s = new String[children.length];
		
		for(int i = 0; i < children.length; ++i) {
			s[i] = children[i].getName(children[i].getNameCount() - 1).toString();
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
			logger.debug("", e);
			return new Path[0];
		}
		
		return dirs.toArray(new Path[dirs.size()]);
	}
	
	@Override
	public WebResource[] listResources(String path) {
		if(path.startsWith("/META-INF") || path.startsWith("/WEB-INF")) {
			logger.debug("  Forbidden path, returning empty");
			return new WebResource[0];
		}

		Path[] children = listDirectory(m_VFS.getFileSystem().getPath(path));
		WebResource[] res = new WebResource[children.length];

		for(int i = 0; i < children.length; ++i) {
			res[i] = new NIOResource(this, m_WebAppPath, children[i]);
		}

		return res;
	}

	@Override
	public boolean mkdir(String path) {
		return false;
	}

	@Override
	public boolean write(String path, InputStream is, boolean overwrite) {
		return false;
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
		return m_Context;
	}

	@Override
	public void setContext(Context context) {
		m_Context = context;
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

	}

	@Override
	public void addLifecycleListener(LifecycleListener listener) {

	}

	@Override
	public LifecycleListener[] findLifecycleListeners() {
		return new LifecycleListener[0];
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {

	}

	@Override
	protected void initInternal() throws LifecycleException {

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
}
