/*
 * RAFTools - Copyright (C) 2016 Zane van Iperen.
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
package net.vs49688.rafview.dds;

/**
 * Seriously, the worst invention since un-sliced bread.
 * This code will kill your family, murder your pets, and steal the soul
 * of your unborn child.
 */
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
