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
