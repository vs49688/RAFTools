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
package net.vs49688.rafview.wwise;

import java.util.*;
import java.nio.*;
import net.vs49688.rafview.wwise.objects.*;

public class HIRC extends Section {

	private final List<WwiseObject> m_Objects;
	
	public HIRC(int type, long length, ByteBuffer buffer) throws WwiseFormatException {
		super(type, length);
		
		long numObjs = (long)buffer.getInt() & 0xFFFFFFFFL;
		
		m_Objects = new ArrayList<>((int)numObjs);
		
		System.err.printf("%d objects\n", numObjs);
		
		for(int i = 0; i < numObjs; ++i) {
			byte id = buffer.get();
			
			/* This includes the objID */
			long objLength = (long)buffer.getInt() & 0xFFFFFFFFL;
			
			/* Calculate the position of the next object so we can jump to it */
			long nextObj = buffer.position() + objLength;
			
			int objID = buffer.getInt();
			
			
			
			if(id == 0x00) {
				m_Objects.add(new SettingsObject(objID, objLength, buffer));
			} else if(id == 0x02) {
				m_Objects.add(new VoiceObject(objID, objLength, buffer));
			} else if(id == 0x03) {
				m_Objects.add(new EventActionObject(objID, objLength, buffer));
			} else if(id == 0x05) {
				// Random Container or Sequence Container, unknown
				m_Objects.add(new WwiseObject(objID, objLength));
			} else if(id == 0x07) {
				// Actor-Mixer Object, unknown
				m_Objects.add(new WwiseObject(objID, objLength));
			} else if(id == 0x0E) {
				// Attenuation Object, unknown
				m_Objects.add(new WwiseObject(objID, objLength));
			} else {
				System.err.printf("Object %d (%d): Type 0x%02x\n", i, objID, id);
				m_Objects.add(new WwiseObject(objID, objLength));
			}
			
			buffer.position((int)nextObj);
		}
	}
	
}
