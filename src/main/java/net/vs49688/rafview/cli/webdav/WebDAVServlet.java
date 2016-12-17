package net.vs49688.rafview.cli.webdav;

import javax.servlet.ServletException;
import org.apache.catalina.servlets.WebdavServlet;

public class WebDAVServlet extends WebdavServlet {

	@Override
	public void init() throws ServletException {
		super.init();
		
		this.listings = true;
		this.debug = 1;
	}
}
