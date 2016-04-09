/*
 * RAFTools - Copyright (C) 2015 Zane van Iperen.
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
