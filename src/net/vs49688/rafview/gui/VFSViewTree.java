/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vs49688.rafview.gui;

import java.awt.event.*;
import javax.swing.*;

public class VFSViewTree extends JTree {
	public VFSViewTree() {
		super();
		this.addMouseListener(new _Mouse());
	}
	
	private class _Mouse implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			if(SwingUtilities.isRightMouseButton(e)) {
				int row = VFSViewTree.this.getClosestRowForLocation(e.getX(), e.getY());
				VFSViewTree.this.setSelectionRow(row);
				//popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			
		}
		
	}
}
