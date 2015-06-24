package net.vs49688.rafview.dds;

/**
 * The worst invention since unsliced bread.
 * Seriously, this code will kill you.
 */
class ARGBColour {
	private int a, r, g, b;

	public void set(int argb) {
		a = (argb & 0xFF000000) >>> 24;
		r = (argb & 0x00FF0000) >>> 16;
		g = (argb & 0x0000FF00) >>> 8;
		b = (argb & 0x000000FF);
	}

	public void set(int index, int value) {
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

	public void setAlpha(int alpha) { a = alpha & 0xFF; }
	public void setRed(int red) { r = red & 0xFF; }
	public void setGreen(int green) { g = green & 0xFF; }
	public void setBlue(int blue) { b = blue & 0xFF; }

	public int getAlpha() { return a; }
	public int getRed() { return r; }
	public int getGreen() { return g; }
	public int getBlue() { return b; }

	public int get() {
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public int get(int index) {
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
}