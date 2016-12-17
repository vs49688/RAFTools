package net.vs49688.rafview.cli.webdav;

import com.sun.org.omg.SendingContext._CodeBaseImplBase;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.vfs.RAFS;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.util.IOUtils;

public class WebDAV implements AutoCloseable {

	private static final Logger logger = LogManager.getFormatterLogger(WebDAV.class);
	private static WebDAV m_sInstance;

	public static WebDAV getInstance() {
		return m_sInstance;
	}

	private final Tomcat m_Tomcat;
	private final Context m_Context;

	private final Properties m_DefaultProperties;
	private final Properties m_Properties;

	public WebDAV(Model model) throws Exception {

		//logger.info("WebDAV (git~%s) Starting up...", GitInfo.getShortCommitHash());
		m_DefaultProperties = generateDefaultProperties();
		m_Properties = loadProperties();
		
		m_Tomcat = new Tomcat();
		
		int port;
		try {
			port = Integer.parseInt(m_Properties.getProperty("esolive.port"), 10);
		} catch(NumberFormatException e) {
			logger.error("Invalid port specified. Exiting...");
			throw e;
		}
		m_Tomcat.setPort(port);
		m_Tomcat.enableNaming();

		initResources(m_Tomcat);

		m_Context = m_Tomcat.addContext("", System.getProperty("user.dir"));
		m_Context.setResources(new VFSResourceRoot("", model.getVFS()));
		Tomcat.addServlet(m_Context, "webdav", new WebDAVServlet());
		m_Context.addServletMapping("/*", "webdav");
	}

	public String getProperty(String property) {
		return m_Properties.getProperty(property);
	}

	@Override
	public void close() throws LifecycleException {
		m_Tomcat.stop();
	}

	public void start() throws LifecycleException {
		m_Tomcat.start();
		m_Tomcat.getServer().await();
	}

	public static void main(String[] args) throws Exception {
		//InputStream is = WebDAV.class.getResourceAsStream("./web.xml");
		
		//String s= IOUtils.toString(new InputStreamReader(is));
		Model model = new Model();
		model.openLolDirectory(Paths.get("E:\\Games\\League of Legends"));
		try(WebDAV esolive = new WebDAV(model)) {
			m_sInstance = esolive;
			esolive.start();
		} catch(Exception e) {
			System.exit(1);
		}
	}

	private void initResources(Tomcat tomcat) {

	}

	private Properties loadProperties() {
		Properties props = new Properties(m_DefaultProperties);

		try(FileInputStream fin = new FileInputStream("esolive.properties")) {
			props.load(fin);
		} catch(IOException e) {
			logger.warn("Unable to load esolive.properties. Using defaults...");
			logger.catching(Level.TRACE, e);
		}
		return props;
	}

	private Properties generateDefaultProperties() {
		Properties props = new Properties();

		props.setProperty("esolive.port", "80");

		return props;
	}
}
