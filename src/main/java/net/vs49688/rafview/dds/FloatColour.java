package net.vs49688.rafview.dds;

public class FloatColour {
	private float a, r, g, b;
	
	public FloatColour() {
		a = 1.0f;
		r = g = b = 0.0f;
	}

	public FloatColour(float a, float r, float g, float b) {
		this.a = a;
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public FloatColour(FloatColour col) {
		set(col);
	}
	
	public final void setAlpha(float alpha) {
		if(alpha < 0.0f || alpha > 1.0f)
			throw new IllegalArgumentException();

		a = alpha;
	}
	
	public final void setRed(float red) {
		if(red < 0.0f || red > 1.0f)
			throw new IllegalArgumentException();

		r = red;
	}
	
	public final void setGreen(float green) {
		if(green < 0.0f || green > 1.0f)
			throw new IllegalArgumentException();

		g = green;
	}
	
	public final void setBlue(float blue) {
		if(b < 0.0f || b > 1.0f)
			throw new IllegalArgumentException();

		b = blue;
	}
	
	public final void set(FloatColour col) {
		a = col.a;
		r = col.r;
		g = col.g;
		b = col.b;
	}
	
	public final void set(int index, float value) {
		if(index < 0 || index > 3)
			throw new IllegalArgumentException("index < 0 || index > 3");

		if(index == 0)
			setAlpha(value);
		else if(index == 1)
			setRed(value);
		else if(index == 2)
			setGreen(value);
		else
			setBlue(value);
	}

	public final float getAlpha() { return a; }
	public final float getRed() { return r; }
	public final float getGreen() { return g; }
	public final float getBlue() { return b; }

	public final float get(int index) {
		if(index < 0 || index > 3)
			throw new IllegalArgumentException("index < 0 || index > 3");

		if(index == 0)
			return getAlpha();
		else if(index == 1)
			return getRed();
		else if(index == 2)
			return getGreen();
		else
			return getBlue();
	}

	public final int toARGB() {
		int r8 = (int)(r * 255) & 0xFF;
		int g8 = (int)(g * 255) & 0xFF;
		int b8 = (int)(b * 255) & 0xFF;
		int a8 = (int)(a * 255) & 0xFF;

		return a8 << 24 | r8 << 16 | g8 << 8 | b8;
	}
}
