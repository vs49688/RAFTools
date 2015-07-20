package net.vs49688.rafview.inibin;

public class Vector3f {
	public float x;
	public float y;
	public float z;
	
	public Vector3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public String toString() {
		return String.format("[%f, %f, %f]", x, y, z);
	}
}
