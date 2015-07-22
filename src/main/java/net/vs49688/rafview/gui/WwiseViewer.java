package net.vs49688.rafview.gui;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import net.vs49688.rafview.sources.*;
import net.vs49688.rafview.wwise.*;

public class WwiseViewer extends JPanel {

	public WwiseViewer(ActionListener listener) {
		initComponents();
		
		setSoundbank("", null);
	}

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_LoadExternalBtn = new javax.swing.JButton();
        m_NameLabel = new javax.swing.JTextField();
        javax.swing.JTabbedPane tabbedPane = new javax.swing.JTabbedPane();
        javax.swing.JPanel audioTab = new javax.swing.JPanel();
        m_AmountLabel = new javax.swing.JLabel();
        javax.swing.JSplitPane audioSplitPane = new javax.swing.JSplitPane();
        javax.swing.JScrollPane audioScrollPane = new javax.swing.JScrollPane();
        m_AudioList = new javax.swing.JList();
        javax.swing.JPanel detailPanel = new javax.swing.JPanel();
        javax.swing.JPanel eventsTab = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        m_LoadExternalBtn.setText("Load External File");
        m_LoadExternalBtn.setActionCommand("inibin->loadexternal");

        m_NameLabel.setEditable(false);
        m_NameLabel.setText("<NAME GOES HERE>");
        m_NameLabel.setOpaque(false);

        m_AmountLabel.setText("%d embedded files.");

        audioScrollPane.setMinimumSize(new java.awt.Dimension(128, 23));
        audioScrollPane.setPreferredSize(new java.awt.Dimension(128, 100));

        m_AudioList.setMaximumSize(new java.awt.Dimension(64, 80));
        m_AudioList.setMinimumSize(new java.awt.Dimension(64, 80));
        audioScrollPane.setViewportView(m_AudioList);

        audioSplitPane.setLeftComponent(audioScrollPane);

        javax.swing.GroupLayout detailPanelLayout = new javax.swing.GroupLayout(detailPanel);
        detailPanel.setLayout(detailPanelLayout);
        detailPanelLayout.setHorizontalGroup(
            detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 460, Short.MAX_VALUE)
        );
        detailPanelLayout.setVerticalGroup(
            detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 339, Short.MAX_VALUE)
        );

        audioSplitPane.setRightComponent(detailPanel);

        javax.swing.GroupLayout audioTabLayout = new javax.swing.GroupLayout(audioTab);
        audioTab.setLayout(audioTabLayout);
        audioTabLayout.setHorizontalGroup(
            audioTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(audioTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(audioTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(audioSplitPane)
                    .addGroup(audioTabLayout.createSequentialGroup()
                        .addComponent(m_AmountLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        audioTabLayout.setVerticalGroup(
            audioTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, audioTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(m_AmountLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(audioSplitPane)
                .addContainerGap())
        );

        tabbedPane.addTab("Audio", audioTab);

        eventsTab.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("There is nothing here yet.");
        eventsTab.add(jLabel1, new java.awt.GridBagConstraints());

        tabbedPane.addTab("Events", eventsTab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(m_NameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(m_LoadExternalBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(tabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_LoadExternalBtn)
                    .addComponent(m_NameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	public final void setSoundbank(String name, Wwise wwise) {
		if(wwise == null) {
			m_NameLabel.setText("");
			m_AmountLabel.setText("0 embedded WEM files.");
			m_AudioList.setModel(new DefaultListModel<>());
			return;
		}
		
		Map<Long, DataSource> wems = wwise.getWEMs();
		
		DefaultListModel<Long> lm = new DefaultListModel<>();
		
		for(final Long l : wems.keySet()) {
			lm.addElement(l);
		}

		m_AudioList.setModel(lm);
		
		m_NameLabel.setText(name);
	}
	
	private void lel() {
		
	}
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel m_AmountLabel;
    private javax.swing.JList m_AudioList;
    private javax.swing.JButton m_LoadExternalBtn;
    private javax.swing.JTextField m_NameLabel;
    // End of variables declaration//GEN-END:variables
}
