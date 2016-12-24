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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import net.vs49688.rafview.inibin.*;

public class InibinViewer extends JPanel {

	private Map<Integer, Value> m_Inibin;
	private Map<String, Map<Integer, String>> m_KeyStrings;
	
	public InibinViewer(ActionListener listener) {
		
		m_KeyStrings = new HashMap<>();
	
		initComponents();

		m_TextArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
		
		m_LoadExternalBtn.addActionListener(listener);
		m_LoadMappingsBtn.addActionListener(listener);

		m_Inibin = null;
		
		setInibin(null);
		setKeyMappings(null);
	}

	public final void setInibin(Map<Integer, Value> inibin) {
		if(inibin == null)
			inibin = new HashMap<>();

		m_Inibin = inibin;
		
		m_TextArea.setText("");
		m_RAWRadio.doClick();
	}
	
	public final void setKeyMappings(Map<String, Map<Integer, String>> keys) {
		if(keys == null) {
			m_KeyStrings = new HashMap<>();
			return;
		}
		
		setInibin(m_Inibin);
		m_KeyStrings = keys;
	}
	
	private void _displayAsRaw(Map<Integer, Value> map) {
		
		m_TextArea.setText("");
		
		for(final Integer key : map.keySet()) {
			m_TextArea.append(String.format("%12d: %s\n", key, map.get(key).toString()));
		}
	}
	
	private void _displayAsMappings(Map<Integer, Value> map) {
		m_TextArea.setText("");
		
		Set<Integer> knownKeys = new HashSet<>();
		Map<String, Map<String, Value>> displayValues = new HashMap<>();
		
		/* Iterate over each key and add it to the correct section (if found) */
		for(final Integer key : map.keySet()) {
			Value val = map.get(key);
			
			for(final String section : m_KeyStrings.keySet()) {
				Map<Integer, String> mappings = m_KeyStrings.get(section);
				
				Map<String, Value> tmp = displayValues.getOrDefault(section, new HashMap<>());
				
				if(mappings.containsKey(key)) {
					tmp.put(mappings.get(key), val);
					knownKeys.add(key);
				}
				
				if(!displayValues.containsKey(section)) {
					displayValues.put(section, tmp);
				}
			}
		}
		
		/* Now add any unknown keys */
		Map<String, Value> tmp = displayValues.getOrDefault("Unknown", new HashMap<>());
		
		/* Subtract the two sets */
		Set<Integer> unknownKeys = new HashSet<>(map.keySet());
		unknownKeys.removeAll(knownKeys);
		
		for(final Integer key : unknownKeys) {
			tmp.put(key.toString(), map.get(key));
		}
		
		if(!displayValues.containsKey("Unknown")) {
			displayValues.put("Unknown", tmp);
		}
		
		/* Now, on our third pass, we can finally fucking display them */
		StringBuilder sb = new StringBuilder();
		for(final String section : displayValues.keySet()) {
			sb.append("[");
			sb.append(section);
			sb.append("]\n");
			
			Map<String, Value> kv = displayValues.get(section);
			
			for(final String key : kv.keySet()) {
				sb.append(key);
				sb.append(" = ");
				sb.append(kv.get(key).toString());
				sb.append("\n");
			}
			
			sb.append("\n");
		}
		
		m_TextArea.setText(sb.toString());
	}

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.ButtonGroup treatGroup = new javax.swing.ButtonGroup();
        m_LoadExternalBtn = new javax.swing.JButton();
        m_RAWRadio = new javax.swing.JRadioButton();
        m_MappingsRadio = new javax.swing.JRadioButton();
        javax.swing.JLabel treatAsLabel = new javax.swing.JLabel();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        m_TextArea = new javax.swing.JTextArea();
        m_LoadMappingsBtn = new javax.swing.JButton();

        m_LoadExternalBtn.setActionCommand("inibin->loadexternal");
        m_LoadExternalBtn.setLabel("Load File");

        treatGroup.add(m_RAWRadio);
        m_RAWRadio.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        m_RAWRadio.setText("Raw");
        m_RAWRadio.setActionCommand("raw");
        m_RAWRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _onTreatmentChange(evt);
            }
        });

        treatGroup.add(m_MappingsRadio);
        m_MappingsRadio.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        m_MappingsRadio.setText("Mappings");
        m_MappingsRadio.setActionCommand("mappings");
        m_MappingsRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _onTreatmentChange(evt);
            }
        });

        treatAsLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        treatAsLabel.setText("Treat as:");

        m_TextArea.setEditable(false);
        m_TextArea.setColumns(20);
        m_TextArea.setRows(5);
        jScrollPane1.setViewportView(m_TextArea);

        m_LoadMappingsBtn.setText("Load Mappings");
        m_LoadMappingsBtn.setActionCommand("inibin->loadmappings");

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
                        .addComponent(m_MappingsRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 136, Short.MAX_VALUE)
                        .addComponent(m_LoadMappingsBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_LoadExternalBtn)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(m_RAWRadio)
                        .addComponent(m_MappingsRadio)
                        .addComponent(treatAsLabel))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(m_LoadExternalBtn)
                        .addComponent(m_LoadMappingsBtn)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void _onTreatmentChange(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__onTreatmentChange
        String cmd = evt.getActionCommand();
		
		if(cmd.equals("mappings")) {
			_displayAsMappings(m_Inibin);
		} else if(cmd.equals("raw")) {
			_displayAsRaw(m_Inibin);
		}
    }//GEN-LAST:event__onTreatmentChange


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton m_LoadExternalBtn;
    private javax.swing.JButton m_LoadMappingsBtn;
    private javax.swing.JRadioButton m_MappingsRadio;
    private javax.swing.JRadioButton m_RAWRadio;
    private javax.swing.JTextArea m_TextArea;
    // End of variables declaration//GEN-END:variables
}
