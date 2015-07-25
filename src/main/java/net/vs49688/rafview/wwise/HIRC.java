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
