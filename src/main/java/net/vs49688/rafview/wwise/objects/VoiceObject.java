package net.vs49688.rafview.wwise.objects;

import java.nio.*;
import net.vs49688.rafview.wwise.WwiseFormatException;

public class VoiceObject extends WwiseObject {

	private final PlayType m_PlayType;
	
	enum PlayType {
		EMBEDDED,
		STREAMED,
		STREAMED_NOLAT
	}
	
	public VoiceObject(int id, long length, ByteBuffer buffer) throws WwiseFormatException {
		super(id, length);
		
		int unknown = buffer.getInt();
		
		int streamed = buffer.getInt();
		if(streamed == 0) {
			m_PlayType = PlayType.EMBEDDED;
		} else if(streamed == 1) {
			m_PlayType = PlayType.STREAMED;
		} else if(streamed == 2) {
			m_PlayType = PlayType.STREAMED_NOLAT;
		} else {
			throw new WwiseFormatException(String.format("Invalid type for voice object. Expected [0, 1, 2], got %d.", streamed));
		}
		
		int audioID = buffer.getInt();
		
		int sourceID = buffer.getInt();
		
		if(m_PlayType == PlayType.EMBEDDED) {
			long wemOffset = (long)buffer.getInt() & 0xFFFFFFFFL;
			long wemLength = (long)buffer.getInt() & 0xFFFFFFFFL;
		}
		
		byte type = buffer.get();
		
		if(type == 0x00) {
			// Sound SFX
		} else if(type == 0x01) {
			// Sound Voice
		}
		
	}
	
}
