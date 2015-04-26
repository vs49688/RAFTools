package net.vs49688.rafview.gui;

import javax.swing.*;
import java.util.*;
import net.vs49688.rafview.inibin.*;

public class InibinViewer extends JPanel {

	private Map<Integer, Value> m_Inibin;
	
	public InibinViewer() {
		initComponents();
		setInibin(null);
	}

	public final synchronized void setInibin(Map<Integer, Value> inibin) {
		if(inibin == null)
			inibin = new HashMap<>();
		
		m_Inibin = inibin;
		
		m_TextArea.setText("");
		m_RAWRadio.setSelected(true);
	}
	
	private void _displayAsRaw(Map<Integer, Value> map) {
		StringBuilder sb = new StringBuilder();
		
		//sb.append("{\n");
		
		for(final Integer key : map.keySet()) {
			final Value val = map.get(key);
			sb.append("%12d: ");
			
			//if(val.getType() == Value.Type.STRING)
			//	sb.append("'");
			
			sb.append(val.toString());
			
			//if(val.getType() == Value.Type.STRING)
			//	sb.append("'");
			
			sb.append("\n");
		}
		
		//sb.append("}\n");
		
		m_TextArea.setText(sb.toString());
	}
	
	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.ButtonGroup treatGroup = new javax.swing.ButtonGroup();
        m_LoadExternalBtn = new javax.swing.JButton();
        m_RAWRadio = new javax.swing.JRadioButton();
        m_ChampionRadio = new javax.swing.JRadioButton();
        m_AbilityRadio = new javax.swing.JRadioButton();
        javax.swing.JLabel treatAsLabel = new javax.swing.JLabel();
        m_ExportBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        m_TextArea = new javax.swing.JTextArea();

        m_LoadExternalBtn.setText("Load External File");

        treatGroup.add(m_RAWRadio);
        m_RAWRadio.setText("Raw");
        m_RAWRadio.setActionCommand("raw");
        m_RAWRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _onTreatmentChange(evt);
            }
        });

        treatGroup.add(m_ChampionRadio);
        m_ChampionRadio.setText("Champion");
        m_ChampionRadio.setActionCommand("champion");
        m_ChampionRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _onTreatmentChange(evt);
            }
        });

        treatGroup.add(m_AbilityRadio);
        m_AbilityRadio.setText("Ability");
        m_AbilityRadio.setActionCommand("ability");
        m_AbilityRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _onTreatmentChange(evt);
            }
        });

        treatAsLabel.setText("Treat as:");

        m_ExportBtn.setText("Export");

        m_TextArea.setEditable(false);
        m_TextArea.setColumns(20);
        m_TextArea.setRows(5);
        jScrollPane1.setViewportView(m_TextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(treatAsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_RAWRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_ChampionRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_AbilityRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                        .addComponent(m_ExportBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_LoadExternalBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(m_RAWRadio)
                        .addComponent(m_ChampionRadio)
                        .addComponent(m_AbilityRadio)
                        .addComponent(treatAsLabel))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(m_LoadExternalBtn)
                        .addComponent(m_ExportBtn)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void _onTreatmentChange(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__onTreatmentChange
        String cmd = evt.getActionCommand();
		
		if(cmd.equals("ability")) {
			System.err.printf("ability\n");
		} else if(cmd.equals("champion")) {
			System.err.printf("champion\n");
		} else if(cmd.equals("raw")) {
			_displayAsRaw(m_Inibin);
		}
    }//GEN-LAST:event__onTreatmentChange


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton m_AbilityRadio;
    private javax.swing.JRadioButton m_ChampionRadio;
    private javax.swing.JButton m_ExportBtn;
    private javax.swing.JButton m_LoadExternalBtn;
    private javax.swing.JRadioButton m_RAWRadio;
    private javax.swing.JTextArea m_TextArea;
    // End of variables declaration//GEN-END:variables
}
