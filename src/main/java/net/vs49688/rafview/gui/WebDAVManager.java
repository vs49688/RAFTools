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
package net.vs49688.rafview.gui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import net.vs49688.rafview.cli.Model;
import net.vs49688.rafview.cli.webdav.StatusListener;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.startup.Tomcat;

public class WebDAVManager extends JPanel {

	private final Model m_Model;

	public WebDAVManager(ActionListener listener, Model model) {
		m_Model = model;
		initComponents();

		/* http://stackoverflow.com/a/5663094 */
		PlainDocument doc = new PlainDocument();
		doc.setDocumentFilter(new DocumentFilter() {
			@Override
			public void insertString(FilterBypass fb, int off, String str, AttributeSet attr)
					throws BadLocationException {
				fb.insertString(off, str.replaceAll("\\D++", ""), attr);  // remove non-digits
			}

			@Override
			public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr)
					throws BadLocationException {
				fb.replace(off, len, str.replaceAll("\\D++", ""), attr);  // remove non-digits
			}
		});
		m_PortField.setDocument(doc);

		m_Model.getWebServer().addListener(new _StatusListener());

		m_StartButton.addActionListener(listener);
		m_StopButton.addActionListener(listener);
		
		m_PortField.setText("80");
	}

	public int getPort() {
		try {
			return Integer.parseInt(m_PortField.getText());
		} catch(NumberFormatException e) {
			return -1;
		}
	}

	private class _StatusListener implements StatusListener {

		@Override
		public void onStart(Tomcat tomcat) {
			m_StartButton.setEnabled(false);
			m_StopButton.setEnabled(true);
			m_PortField.setEditable(false);

			tomcat.getServer().addLifecycleListener(new _StatusUpdater(m_ServerStatus));
			tomcat.getConnector().addLifecycleListener(new _StatusUpdater(m_ConnectorStatus));
			tomcat.getEngine().addLifecycleListener(new _StatusUpdater(m_EngineStatus));
			tomcat.getHost().addLifecycleListener(new _StatusUpdater(m_HostStatus));
			tomcat.getService().addLifecycleListener(new _StatusUpdater(m_ServiceStatuus));

			m_PortField.setText(Integer.toString(tomcat.getConnector().getPort()));
		}

		@Override
		public void onStop(Tomcat tomcat) {
			m_StartButton.setEnabled(true);
			m_StopButton.setEnabled(false);
			m_PortField.setEditable(true);
		}
	}

	private static class _StatusUpdater implements LifecycleListener, Runnable {

		private final JLabel m_Text;

		private LifecycleEvent m_Event = null;

		public _StatusUpdater(JLabel label) {
			m_Text = label;
		}

		@Override
		public void lifecycleEvent(LifecycleEvent event) {
			m_Event = event;
			SwingUtilities.invokeLater(this);
		}

		@Override
		public void run() {
			if(m_Event == null) {
				return;
			}
			m_Text.setText(m_Event.getLifecycle().getStateName());
		}
	}

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel webdavPanel = new javax.swing.JPanel();
        m_StartButton = new javax.swing.JButton();
        m_StopButton = new javax.swing.JButton();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        m_ServerStatus = new javax.swing.JLabel();
        javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
        m_PortField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel6 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel9 = new javax.swing.JLabel();
        m_ConnectorStatus = new javax.swing.JLabel();
        m_EngineStatus = new javax.swing.JLabel();
        m_HostStatus = new javax.swing.JLabel();
        m_ServiceStatuus = new javax.swing.JLabel();

        m_StartButton.setText("Start");
        m_StartButton.setActionCommand("webdav->start");
        m_StartButton.setMaximumSize(new java.awt.Dimension(85, 32));
        m_StartButton.setMinimumSize(new java.awt.Dimension(85, 32));
        m_StartButton.setPreferredSize(new java.awt.Dimension(85, 32));

        m_StopButton.setText("Stop");
        m_StopButton.setActionCommand("webdav->stop");
        m_StopButton.setMaximumSize(new java.awt.Dimension(85, 32));
        m_StopButton.setMinimumSize(new java.awt.Dimension(85, 32));
        m_StopButton.setPreferredSize(new java.awt.Dimension(85, 32));

        jLabel1.setText("WebDAV Settings:");

        jLabel2.setText("Server:");

        m_ServerStatus.setText(" ");

        jLabel7.setText("Port:");

        m_PortField.setText("80");

        jLabel3.setText("Connector:");

        jLabel6.setText("Engine:");

        jLabel8.setText("Host:");

        jLabel9.setText("Service:");

        m_ConnectorStatus.setText(" ");
        m_ConnectorStatus.setToolTipText("");

        m_EngineStatus.setText(" ");

        m_HostStatus.setText(" ");

        m_ServiceStatuus.setText(" ");

        javax.swing.GroupLayout webdavPanelLayout = new javax.swing.GroupLayout(webdavPanel);
        webdavPanel.setLayout(webdavPanelLayout);
        webdavPanelLayout.setHorizontalGroup(
            webdavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(webdavPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(webdavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(webdavPanelLayout.createSequentialGroup()
                        .addGroup(webdavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(webdavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_PortField)
                            .addComponent(m_ServiceStatuus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_HostStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_EngineStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_ServerStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_ConnectorStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(webdavPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(webdavPanelLayout.createSequentialGroup()
                        .addComponent(m_StartButton, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                        .addComponent(m_StopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        webdavPanelLayout.setVerticalGroup(
            webdavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, webdavPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webdavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(m_ServerStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webdavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(m_ConnectorStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webdavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(m_EngineStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webdavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(m_HostStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webdavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(m_ServiceStatuus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webdavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_PortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(webdavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_StartButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_StopButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(webdavPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 352, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(webdavPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 145, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel m_ConnectorStatus;
    private javax.swing.JLabel m_EngineStatus;
    private javax.swing.JLabel m_HostStatus;
    private javax.swing.JTextField m_PortField;
    private javax.swing.JLabel m_ServerStatus;
    private javax.swing.JLabel m_ServiceStatuus;
    private javax.swing.JButton m_StartButton;
    private javax.swing.JButton m_StopButton;
    // End of variables declaration//GEN-END:variables
}
