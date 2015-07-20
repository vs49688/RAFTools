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
package net.vs49688.rafview.dds;

import java.awt.image.*;
import java.nio.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.vs49688.rafview.gui.NavigableImagePanel;

/* Resources:
 * http://www.matejtomcik.com/Public/KnowHow/DXTDecompression/
 * http://www.fsdeveloper.com/wiki/index.php?title=DXT_compression_explained
 * https://msdn.microsoft.com/en-us/library/windows/desktop/bb694531(v=vs.85).aspx
 */
public class DDSUtils {

	private static final Map<Integer, PixelTransform> m_Transforms = _createTransformMap();
	
	private static Map<Integer, PixelTransform>  _createTransformMap() {
		HashMap<Integer, PixelTransform> map = new HashMap<>();
		
		map.put(DDSImage.D3DFMT_R5G6B5, (PixelTransform) (ByteBuffer bb) -> {
			short rawPixel = bb.getShort();
			
			int r5 = (rawPixel & 0xF800) >>> 11;
			int g5 = (rawPixel & 0x07E0) >>> 5;
			int b5 = (rawPixel & 0x001F);
			
			float r = 1.0f/31.0f * r5;
			float g = 1.0f/63.0f * g5;
			float b = 1.0f/31.0f * b5;
			
			return new FloatColour(1.0f, r, g, b);
		});
		
		map.put(DDSImage.D3DFMT_A1R5G5B5, (PixelTransform) (ByteBuffer bb) -> {
			short rawPixel = bb.getShort();

			int r5 = (rawPixel & 0x7C00) >>> 10;
			int g5 = (rawPixel & 0x03E0) >>> 5;
			int b5 = (rawPixel & 0x001F);
			
			float a = (rawPixel & 0x8000) == 0 ? 0.0f : 1.0f;
			float r = 1.0f/31.0f * r5;
			float g = 1.0f/31.0f * g5;
			float b = 1.0f/31.0f * b5;
			
			return new FloatColour(a, r, g, b);
		});

		map.put(DDSImage.D3DFMT_X1R5G5B5, (PixelTransform) (ByteBuffer bb) -> {
			short rawPixel = bb.getShort();

			int r5 = (rawPixel & 0x7C00) >>> 10;
			int g5 = (rawPixel & 0x03E0) >>> 5;
			int b5 = (rawPixel & 0x001F);
			
			float r = 1.0f/31.0f * r5;
			float g = 1.0f/31.0f * g5;
			float b = 1.0f/31.0f * b5;
			
			return new FloatColour(1.0f, r, g, b);
		});

		map.put(DDSImage.D3DFMT_R8G8B8, (PixelTransform) (ByteBuffer bb) -> {
			int p = _read_le24(bb);
			
			float r = ((p & 0x00FF0000) >> 16) / 255.0f;
			float g = ((p & 0x0000FF00) >>  8) / 255.0f;
			float b = ((p & 0x000000FF) >>  0) / 255.0f;
			
			return new FloatColour(1.0f, r, g, b);
		});

		map.put(DDSImage.D3DFMT_A8R8G8B8, (PixelTransform) (ByteBuffer bb) -> {
			int p = bb.getInt();
			
			float a = ((p & 0xFF000000) >> 24) / 255.0f;
			float r = ((p & 0x00FF0000) >> 16) / 255.0f;
			float g = ((p & 0x0000FF00) >>  8) / 255.0f;
			float b = ((p & 0x000000FF) >>  0) / 255.0f;
			
			return new FloatColour(a, r, g, b);
		});
		
		map.put(DDSImage.D3DFMT_X8R8G8B8, (PixelTransform) (ByteBuffer bb) -> {
			int p = bb.getInt();

			float r = ((p & 0x00FF0000) >> 16) / 255.0f;
			float g = ((p & 0x0000FF00) >>  8) / 255.0f;
			float b = ((p & 0x000000FF) >>  0) / 255.0f;
			
			return new FloatColour(1.0f, r, g, b);
		});
		
		map.put(DDSImage.D3DFMT_A8B8G8R8, (PixelTransform) (ByteBuffer bb) -> {
			int p = bb.getInt();
			
			float a = ((p & 0xFF000000) >> 24) / 255.0f;
			float b = ((p & 0x00FF0000) >> 16) / 255.0f;
			float g = ((p & 0x0000FF00) >>  8) / 255.0f;
			float r = ((p & 0x000000FF) >>  0) / 255.0f;
			
			return new FloatColour(a, r, g, b);
		});
		
		map.put(DDSImage.D3DFMT_X8B8G8R8, (PixelTransform) (ByteBuffer bb) -> {
			int p = bb.getInt();

			float b = ((p & 0x00FF0000) >> 16) / 255.0f;
			float g = ((p & 0x0000FF00) >>  8) / 255.0f;
			float r = ((p & 0x000000FF) >>  0) / 255.0f;
			
			return new FloatColour(1.0f, r, g, b);
		});
		return map;
	}
	
	/**
	 * Convert a DDS image into a A8R8G8B8 BufferedImage suitable for use in Swing.
	 * @param dds The DDS image.
	 * @param image The structure containing infomation about the specific mipmap
	 * to be converted.
	 * @return A A8R8G8B8 BufferedImage suitable for use in Swing.
	 */
	public static BufferedImage getSwingImage(DDSImage dds, DDSImage.ImageInfo image) {
		
		int pixelFormat = dds.getPixelFormat();
		
		//dds.debugPrint();
		switch(pixelFormat) {
			case DDSImage.D3DFMT_R5G6B5:
			case DDSImage.D3DFMT_R8G8B8:
			case DDSImage.D3DFMT_A1R5G5B5:
			case DDSImage.D3DFMT_X1R5G5B5:
			case DDSImage.D3DFMT_A8R8G8B8:
			case DDSImage.D3DFMT_X8R8G8B8:
			case DDSImage.D3DFMT_A8B8G8R8:
			case DDSImage.D3DFMT_X8B8G8R8:
				return parseUncompressed(image, m_Transforms.get(pixelFormat));

			case DDSImage.D3DFMT_DXT1:
				return _parseDXT(image, 1);
			case DDSImage.D3DFMT_DXT2:
				return _parseDXT(image, 2);
			case DDSImage.D3DFMT_DXT3:
				return _parseDXT(image, 3);
			case DDSImage.D3DFMT_DXT4:
				return _parseDXT(image, 4);
			case DDSImage.D3DFMT_DXT5:
				return _parseDXT(image, 5);

			default:
				return null;
		}
	}
	
	/** Read a 24-bit, little-endian integer. */
	private static int _read_le24(ByteBuffer bb) {
		return (bb.get() & 0xFF) | ((bb.get() & 0xFF) << 8) | ((bb.get() & 0xFF) << 16);
	}
	
	/**
	 * Parse an uncompressed DDS image into a A8R8G8B8 BufferedImage.
	 * @param image The DDS Image to convert.
	 * @param transform The pixel transformation class corresponding the the
	 * pixel format of the image.
	 * @return A A8R8G8B8 BufferedImage capable for use in Swing.
	 */
	private static BufferedImage parseUncompressed(DDSImage.ImageInfo image, PixelTransform transform) {
		BufferedImage bimg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		ByteBuffer rawData = image.getData();

		rawData.order(ByteOrder.LITTLE_ENDIAN);

		for(int j = 0; j < bimg.getHeight(); ++j) {
			for(int i = 0; i < bimg.getWidth(); ++i) {
				bimg.setRGB(i, j, transform.getPixel(rawData).toARGB());
			}
		}
		
		return bimg;
	}

	private static class DXTBlock {
		public final float[] alphaTable;
		public final int[] alphaIndices;
		public final FloatColour[] colours;

		public DXTBlock() {
			colours = new FloatColour[4];
			for(int i = 0; i < colours.length; ++i)
				colours[i] = new FloatColour();
			
			alphaTable = new float[16];
			alphaIndices = new int[16];
		}
	}
	
	/**
	 * Parse an DXT-compressed image into a A8R8G8B8 BufferedImage.
	 * @param image The DDS Image to convert.
	 * @param version The version of DXT to parse, in the range [1, 5].
	 * @return A A8R8G8B8 BufferedImage capable for use in Swing.
	 */
	private static BufferedImage _parseDXT(DDSImage.ImageInfo image, int version) {
		BufferedImage bimg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		PixelTransform rgb565 = m_Transforms.get(DDSImage.D3DFMT_R5G6B5);
		
		ByteBuffer rawData = image.getData();
		
		rawData.order(ByteOrder.LITTLE_ENDIAN);
		
		DXTBlock block = new DXTBlock();
		
		for(int j = 0; j < image.getHeight()/4; ++j) {
			for(int i = 0; i < image.getWidth()/4; ++i) {
				
				/* DXT2 and DXT3 store 16 bits (4bpp * 16) alpha channel */
				if(version == 2 || version == 3) {
					/* Read the alphas into the table */
					_parseAlphaTable(block.alphaTable, rawData.getLong());
					
					/* Create a 1:1 mapping of the pixel to the table.
					 * This essentially converts the format to DXT4/5
					 * (except with indices > 7), so I don't have to write
					 * special code. */
					for(int k = 0; k < 16; ++k) {
						block.alphaIndices[k] = k;
					}
				/* DXT4 and DXT5 store two 8-bit alpha values,
				 * followed by a 48-bit table (3bpp * 16) indices */
				} else if(version == 4 || version == 5) {
					
					/* Calculate the interpolated alphas from the two endpoints */
					interpolateAlphas(block.alphaTable, rawData);
					
					/* Build the index table */
					buildIndexTable(block.alphaIndices, rawData);
				}
				
				block.colours[0].set(rgb565.getPixel(rawData));
				block.colours[1].set(rgb565.getPixel(rawData));

				if(version == 1) {
					_calcDXT1ColComponent(block);
				} else {
					_calcDXTNot1ColComponent(block);
				}

				applyTable(i, j, rawData.getInt(), block, bimg, version != 1, version % 2 == 0);
			}
		}
		
		return bimg;
	}

	/**
	 * Apply the DXT colour index table to the image.
	 * @param i The x position in the (image width/4).
	 * @param j The y position in the (image height/4).
	 * @param table The index table to apply to the image.
	 * @param block The DXTBlock containing the colour and alpha data.
	 * @param bimg The BufferedImage to receive the pixels.
	 * @param useAlphaTable Do we use the DXTBlock's alpha table, or the alpha
	 * value already in the colour. (Should be true for anything that's not
	 * DXT1).
	 * @param premultiplied Are the colours premultiplied by their alpha?
	 */
	private static void applyTable(int i, int j, int table, DXTBlock block, BufferedImage bimg, boolean useAlphaTable, boolean premultiplied) {
		
		int mask = 0b11;
		
		for(int x = 0, k = 0; x < 4; ++x) for(int y = 0; y < 4; ++y, ++k) {
			FloatColour pixel = block.colours[(table & (mask << (2*k))) >>> (2*k)];
			
			if(useAlphaTable) {
				
				float alpha = block.alphaTable[block.alphaIndices[k]];
				pixel.setAlpha(alpha);
			}
			
			if(premultiplied) {
				for(int w = 1; i <= 3; ++i)
					pixel.set(w, pixel.get(w)/pixel.getAlpha());
			}
			bimg.setRGB(i*4 + y, j*4 + x, pixel.toARGB());
		}
	}

	/**
	 * Parse a DXT2/3 alpha table into an array.
	 * @param alphas The array to write the alpha values into.
	 * @param rawTable The raw table.
	 */
	private static void _parseAlphaTable(float[] alphas, long rawTable) {
		
		long mask = 0b1111;
		
		for(int i = 0; i < 16; ++i) {
			int alpha4 = (int)((rawTable & (mask << (long)(4*i))) >>> (4*i));
			alphas[i] = 1.0f/15.0f * alpha4;
		}
	}
	
	/**
	 * Calculate the 8 interpolated alpha values from the two at the start of
	 * the block.
	 * @param alphas The array to receive the alpha values.
	 * @param bb The buffer containing the data.
	 */
	private static void interpolateAlphas(float[] alphas, ByteBuffer bb) {
		
		int alpha0 = bb.get() & 0xFF;
		int alpha1 = bb.get() & 0xFF;
		
		alphas[0] = alpha0 / 255.0f;
		alphas[1] = alpha1 / 255.0f;

		if(alphas[0] > alphas[1]) {
			alphas[2] = ((6 * alpha0 + 1 * alpha1) / 7.0f) / 255.0f;
			alphas[3] = ((5 * alpha0 + 2 * alpha1) / 7.0f) / 255.0f;
			alphas[4] = ((4 * alpha0 + 3 * alpha1) / 7.0f) / 255.0f;
			alphas[5] = ((3 * alpha0 + 4 * alpha1) / 7.0f) / 255.0f;
			alphas[6] = ((2 * alpha0 + 5 * alpha1) / 7.0f) / 255.0f;
			alphas[7] = ((1 * alpha0 + 6 * alpha1) / 7.0f) / 255.0f;
		} else {
			alphas[2] = ((4 * alpha0 + 1 * alpha1) / 5.0f) / 255.0f;
			alphas[3] = ((3 * alpha0 + 2 * alpha1) / 5.0f) / 255.0f;
			alphas[4] = ((2 * alpha0 + 3 * alpha1) / 5.0f) / 255.0f;
			alphas[5] = ((1 * alpha0 + 4 * alpha1) / 5.0f) / 255.0f;
			alphas[6] = 0.0f;
			alphas[7] = 1.0f;
		}
	}
	
	/**
	 * Build the alpha index table for a DXT4/5 image.
	 * @param idx The array to receive the indices.
	 * @param bb The buffer containing the index table.
	 */
	private static void buildIndexTable(int[] idx, ByteBuffer bb) {
		final byte[] raw = new byte[6];

		bb.get(raw);

		idx[ 0] = (raw[0] & 0b00000111);
		idx[ 1] = (raw[0] & 0b00111000) >>> 3;
		idx[ 2] = (raw[0] & 0b11000000) >>> 6 |
				  (raw[1] & 0b00000001) <<  2;
		
		idx[ 3] = (raw[1] & 0b00001110) >>> 1;
		
		
		idx[ 4] = (raw[1] & 0b01110000) >>> 4;
		idx[ 5] = (raw[1] & 0b10000000) >>> 7 |
				  (raw[2] & 0b00000011) <<  1;
		
		idx[ 6] = (raw[2] & 0b00011100) >>> 2;
		idx[ 7] = (raw[2] & 0b11100000) >>> 5;

		idx[ 8] = (raw[3] & 0b00000111);
		idx[ 9] = (raw[3] & 0b00111000) >>> 3;
		idx[10] = (raw[3] & 0b11000000) >>> 6 |
				  (raw[4] & 0b00000001) <<  2;
		
		idx[11] = (raw[4] & 0b00001110) >>> 1;
		
		
		idx[12] = (raw[4] & 0b01110000) >>> 4;
		idx[13] = (raw[4] & 0b10000000) >>> 7 |
				  (raw[5] & 0b00000011) <<  1;
		
		idx[14] = (raw[5] & 0b00011100) >>> 2;
		idx[15] = (raw[5] & 0b11100000) >>> 5;
	}

	private static void _calcDXTNot1ColComponent(DXTBlock block) {
		FloatColour cols[] = block.colours;

		for(int i = 1; i <= 3; ++i) {
			cols[2].set(i, (2 * cols[0].get(i) + cols[1].get(i)) / 3);
			cols[3].set(i, (2 * cols[1].get(i) + cols[0].get(i)) / 3);
		}
		
		cols[0].setAlpha(0.0f);
		cols[1].setAlpha(0.0f);
		cols[2].setAlpha(0.0f);
		cols[3].setAlpha(0.0f);
	}
	
	private static void _calcDXT1ColComponent(DXTBlock block) {
		
		FloatColour cols[] = block.colours;

		int col0ARGB = cols[0].toARGB();
		int col1ARGB = cols[1].toARGB();
		
		if(col0ARGB > col1ARGB)
			cols[3].setAlpha(1.0f);
		else
			cols[3].setAlpha(0.0f);

		cols[0].setAlpha(1.0f);
		cols[1].setAlpha(1.0f);
		cols[2].setAlpha(1.0f);
		
		for(int i = 1; i <= 3; ++i) {
			if(col0ARGB > col1ARGB) {
				cols[2].set(i, (2.0f * cols[0].get(i) + cols[1].get(i)) / 3.0f);
				cols[3].set(i, (2.0f * cols[1].get(i) + cols[0].get(i)) / 3.0f);
			} else {
				cols[2].set(i, (cols[0].get(i) + cols[1].get(i)) / 2.0f);
				cols[3].set(i, 0x00);
			}
		}
	}


	private interface PixelTransform {
		/**
		 * Read a pixel from the buffer and convert it to A8R8G8B8.
		 * @param bb The buffer containing the pixel.
		 * @return A pixel.
		 */
		public FloatColour getPixel(ByteBuffer bb);
	}
	
	public static void main(String[] args) throws Exception {
		DDSImage img = DDSImage.read("E:\\Zane\\Desktop\\Kalista_Skin01_R_alphaslice.dds");
		//DDSImage img = DDSImage.read("E:\\Zane\\Desktop\\sample.dds");
		
		JFrame frame = new JFrame("DDSUtils Test");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		net.vs49688.rafview.gui.NavigableImagePanel panel = new NavigableImagePanel(getSwingImage(img, img.getMipMap(0)));
		
		frame.add(panel);
		SwingUtilities.invokeLater(() -> { frame.setVisible(true); });
	}
}
