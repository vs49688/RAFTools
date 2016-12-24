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

import net.vs49688.rafview.cli.Model;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import org.apache.catalina.LifecycleException;
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
	private final Model m_Model;
	
	private Tomcat m_Tomcat;
	
	private int m_Port;

	public WebDAV(Model model) {
		m_Model = model;
		m_Port = DEFAULT_PORT;
	}

	public void setPort(int port) {
		if(port <= 0 || port > 65535) {
			throw new IllegalArgumentException();
		}

		m_Port = port;
	}

	@Override
	public void close() throws LifecycleException {
		//m_Server.stop();
		if(m_Tomcat != null) {
			m_Tomcat.stop();
			m_Tomcat.destroy();
		}
	
		m_Tomcat = null;
	}

	public void start() throws LifecycleException {

		if(m_Tomcat != null) {
			return;
		}

		m_Tomcat = new Tomcat();
		m_Tomcat.setPort(m_Port);

		Context ctx = m_Tomcat.addContext("", "/");
		ctx.setResources(new RAFSResourceRoot("", m_Model.getVFS()));
		Tomcat.addServlet(ctx, "webdav", new WebDAVServlet());
		ctx.addServletMapping("/*", "webdav");

		m_Tomcat.start();
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
