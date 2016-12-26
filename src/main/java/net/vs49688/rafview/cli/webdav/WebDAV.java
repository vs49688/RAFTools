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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.vs49688.rafview.cli.Model;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebDAV implements AutoCloseable {

	/* Notes:
	 * Tomcat is fragile as fuck. Contexts aren't run after a start()/stop()
	 * cycle, and a lot of the internals aren't documented. Which is fine,
	 * as you're not really meant to be screwing with them.
	 *
	 * This just uses the provided org.apache.catalina.startup.Tomcat
	 * wrapper class and creates/destroys it when needed ^-^
	 */
	private static final Logger logger = LogManager.getFormatterLogger(WebDAV.class);
	private static final int DEFAULT_PORT = 80;
	private static final Path BASE_DIR = Paths.get(String.format("%s/raftools-tmp", System.getProperty("java.io.tmpdir")));
	private final Model m_Model;
	private final List<StatusListener> m_Listeners;

	private Tomcat m_Tomcat;

	private int m_Port;

	public WebDAV(Model model) {
		m_Model = model;
		m_Port = DEFAULT_PORT;
		m_Listeners = new ArrayList<>();
	}

	public void addListener(StatusListener l) {
		if(l == null) {
			return;
		}

		m_Listeners.add(l);
	}

	public void removeListener(StatusListener l) {
		m_Listeners.remove(l);
	}

	public void setPort(int port) {
		if(port <= 0 || port > 65535) {
			throw new IllegalArgumentException();
		}

		m_Port = port;
	}

	@Override
	public void close() throws LifecycleException {
		if(m_Tomcat != null) {

			m_Tomcat.stop();
			m_Tomcat.destroy();

			for(StatusListener l : m_Listeners) {
				l.onStop(m_Tomcat);
			}

			try {
				Files.delete(BASE_DIR);
			} catch(IOException e) {
				/* We really don't care if this fails. */
			}
		}

		m_Tomcat = null;
	}

	public void start() throws LifecycleException {
		if(m_Tomcat != null) {
			return;
		}

		m_Tomcat = new Tomcat();
		m_Tomcat.setPort(m_Port);
		m_Tomcat.setBaseDir(BASE_DIR.toString());

		Context ctx = m_Tomcat.addContext("", "/");
		ctx.setResources(new RAFSResourceRoot("", m_Model.getVFS()));
		Tomcat.addServlet(ctx, "webdav", new WebDAVServlet());
		ctx.addServletMapping("/*", "webdav");

		for(StatusListener l : m_Listeners) {
			l.onStart(m_Tomcat);
		}

		m_Tomcat.start();
	}

	public Tomcat getTomcat() {
		return m_Tomcat;
	}

//	public static void main(String[] args) throws Exception {
//
//		Model model = new Model();
//		model.openLolDirectory(java.nio.file.Paths.get("E:/Games/League of Legends"));
//		try(WebDAV webdav = new WebDAV(model)) {
//			webdav.start();
//			webdav.m_Tomcat.getServer().await();
//		}
//	}
}
