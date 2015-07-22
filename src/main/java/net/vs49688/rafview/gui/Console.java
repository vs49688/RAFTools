/*
 * RAFTools - Copyright (C) 2015 Zane van Iperen.
 *    Contact: zane.vaniperen@uqconnect.edu.au
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

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class Console extends JPanel implements Appendable {

	private final JTextArea m_Text;
	private final JButton m_OKBtn;
	private final JTextField m_Command;

	public Console(ActionListener listener) {
		super();
		this.setLayout(new BorderLayout());

		m_Text = new JTextArea();
		m_Text.setEditable(false);
		m_Text.setFont(new Font("Monospaced", Font.PLAIN, 13));

		JScrollPane sPane = new JScrollPane();
		sPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		sPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		sPane.setViewportView(m_Text);

		this.add(sPane, BorderLayout.CENTER);

		JPanel conPanel = new JPanel();
		conPanel.setLayout(new GridBagLayout());
		this.add(conPanel, BorderLayout.PAGE_END);

		GridBagConstraints gbc = new GridBagConstraints();
		Insets ins = new Insets(2, 2, 2, 2);
		gbc.insets = ins;
		m_Command = new JTextField();
		gbc.gridx = gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0f;
		conPanel.add(m_Command, gbc);

		m_OKBtn = new JButton("Submit");
		m_OKBtn.setActionCommand("console->submit");

		m_OKBtn.addActionListener(new _Echo());
		m_OKBtn.addActionListener(listener);
		gbc.gridx = 1;
		gbc.weightx = 0.0f;
		conPanel.add(m_OKBtn, gbc);
		
	}

	public PrintStream getStream() {
		return new PrintStream(new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				append((char)b);
			}
			
			@Override
			public void write(byte[] b) throws IOException {
				append(new String(b));
			}
		});
	}
	/*public void submit() {

		if(m_Interpreter != null) {
			Interpreter.CommandResult result = m_Interpreter.executeCommand(cmd);
			
			while(result.getState() != Interpreter.CommandResult.State.COMPLETE) {
				
			}
			
			Exception e = result.getException();
			
			if(e != null) {
				log(e.getMessage());
			}
		}
	}*/
	
	/*private synchronized void print(String message) {
		if(message == null)
			return;
		
		m_Text.append(String.format("%s\n", message));
	}*/

	@Override
	public synchronized Appendable append(CharSequence csq) {
		print((String)csq);
		return this;
	}

	@Override
	public synchronized Appendable append(CharSequence csq, int start, int end) {
		print((String)csq.subSequence(start, end));
		return this;
	}

	@Override
	public Appendable append(char c) {
		print(String.valueOf(c));
		return this;
	}	
	
	public synchronized void print(String s) {
		((DefaultCaret)m_Text.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		m_Text.append(s);
	}
	
	public void println(String s) {
		print(s + "\n");
		m_Text.setCaretPosition(m_Text.getDocument().getLength());
	}

	public String getCommandText() {
		return m_Command.getText();
	}
	
	public JButton getSubmitButton() {
		return m_OKBtn;
	}
	
	private class _Echo implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = m_Command.getText().trim();
			if(cmd.equals("")) {
				return;
			}

			println(String.format("] %s", cmd));
			m_Command.setText("");
		}
		
	}
}
