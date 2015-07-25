package net.vs49688.rafview.gui;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import net.vs49688.rafview.sources.*;
import net.vs49688.rafview.wwise.*;

public class WwiseViewer extends JPanel {

	private final OpHandler m_OpHandler;
	private Wwise m_CurrentFile;
	
	public WwiseViewer(ActionListener listener, OpHandler handler) {
		
		m_OpHandler = handler;
	
		initComponents();
		
		m_ExtractBtn.addActionListener(listener);
		m_LoadExternalBtn.addActionListener(listener);
		m_AudioList.addMouseListener(new _RightClickListener());
		m_AudioList.addListSelectionListener(new _Selection());
		m_AudioList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        m_ExtractBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        javax.swing.JPanel eventsTab = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();

        m_LoadExternalBtn.setText("Load External File");
        m_LoadExternalBtn.setActionCommand("wwise->loadexternal");

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

        m_ExtractBtn.setText("Extract");
        m_ExtractBtn.setEnabled(false);

        jLabel2.setText("TODO: Display WEM info.");

        javax.swing.GroupLayout detailPanelLayout = new javax.swing.GroupLayout(detailPanel);
        detailPanel.setLayout(detailPanelLayout);
        detailPanelLayout.setHorizontalGroup(
            detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, detailPanelLayout.createSequentialGroup()
                .addContainerGap(503, Short.MAX_VALUE)
                .addComponent(m_ExtractBtn)
                .addContainerGap())
            .addGroup(detailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        detailPanelLayout.setVerticalGroup(
            detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, detailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 320, Short.MAX_VALUE)
                .addComponent(m_ExtractBtn)
                .addContainerGap())
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

        jLabel1.setText("TODO: Implement Wwise event parsing.");

        javax.swing.GroupLayout eventsTabLayout = new javax.swing.GroupLayout(eventsTab);
        eventsTab.setLayout(eventsTabLayout);
        eventsTabLayout.setHorizontalGroup(
            eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(532, Short.MAX_VALUE))
        );
        eventsTabLayout.setVerticalGroup(
            eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(398, Short.MAX_VALUE))
        );

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
                .addComponent(tabbedPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_LoadExternalBtn)
                    .addComponent(m_NameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	public final void setSoundbank(String name, Wwise wwise) {
		if(wwise == null) {
			m_CurrentFile = null;
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
		m_AmountLabel.setText(String.format("%d embedded WEM files.", wems.size()));
		
		m_CurrentFile = wwise;
	}

	private class _RightClickListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {

			if(SwingUtilities.isRightMouseButton(e)) {
				int row = m_AudioList.locationToIndex(e.getPoint());
				m_AudioList.setSelectedIndex(row);
			}
			Long l = (Long)m_AudioList.getSelectedValue();
			if(l == null)
				return;

			if(SwingUtilities.isRightMouseButton(e)) {
				JPopupMenu m_ContextMenu = createPopupMenu(l, m_CurrentFile);
				m_AudioList.add(m_ContextMenu);
				m_ContextMenu.show(e.getComponent(), e.getX(), e.getY());
				
			}
		}
	}
	
	private JPopupMenu createPopupMenu(Long id, Wwise wwise) {
		JPopupMenu m = new JPopupMenu();
		
		JMenuItem item = new JMenuItem("Extract");
		item.addActionListener((ActionEvent ae) -> {
			m_OpHandler.onExtract(id, wwise);
		});
		m.add(item);
		
		item = new JMenuItem("Extract All");
		item.addActionListener((ActionEvent ae) -> {
			m_OpHandler.onExtractAll(wwise);
		});
		m.add(item);

		return m;
	}

	private class _Selection implements ListSelectionListener {	

		@Override
		public void valueChanged(ListSelectionEvent lse) {
			if(!lse.getValueIsAdjusting())
				return;
			
			Long l = (Long)m_AudioList.getSelectedValue();
			if(l == null)
				return;
			
			m_OpHandler.onSelect(l, m_CurrentFile);
		}
	}
	public interface OpHandler {
		public void onSelect(Long id, Wwise wwise);
		public void onExtract(Long id, Wwise wwise);
		public void onExtractAll(Wwise wwise);
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel m_AmountLabel;
    private javax.swing.JList m_AudioList;
    private javax.swing.JButton m_ExtractBtn;
    private javax.swing.JButton m_LoadExternalBtn;
    private javax.swing.JTextField m_NameLabel;
    // End of variables declaration//GEN-END:variables
}
