package net.vs49688.rafview.wwise.objects;

// Will be used when I can be bothered parsing Wwise events.
public class WwiseObject {
	private final long m_Length;
	private final int m_ID;
	
	/**
	 * Get the globally-unique ID of this object.
	 * This ie equivalent to calling hashCode()
	 * @return 
	 */
	public int getID() {
		return hashCode();
	}
	
	public WwiseObject(int id, long length) {
		m_Length = length;
		m_ID = id;
	}
	
	@Override
	public int hashCode() {
		return m_ID;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final WwiseObject other = (WwiseObject) obj;
		if (this.m_ID != other.m_ID) {
			return false;
		}
		return true;
	}
}
