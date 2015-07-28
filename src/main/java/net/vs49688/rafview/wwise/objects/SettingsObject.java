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
package net.vs49688.rafview.wwise.objects;

import java.util.*;
import java.nio.*;

// UNTESTED
public class SettingsObject extends WwiseObject {
	private final List<AbstractMap.SimpleImmutableEntry> m_Settings;
	
	public SettingsObject(int id, long length, ByteBuffer buffer) {
		super(id, length);
		
		int numSettings = buffer.get() & 0xFF;
		
		List<Byte> keys = new ArrayList<>();
		List<Float> values = new ArrayList<>();
		
		for(int i = 0; i < numSettings; ++i) {
			keys.add(buffer.get());
		}
		
		for(int i = 0; i < numSettings; ++i) {
			values.add(buffer.getFloat());
		}
		
		m_Settings = new ArrayList<>(keys.size());
		
		for(int i = 0; i < numSettings; ++i) {
			m_Settings.add(new AbstractMap.SimpleImmutableEntry(keys.get(i), values.get(i)));
		}
	}
	
}
