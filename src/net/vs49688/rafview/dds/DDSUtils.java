package net.vs49688.rafview.dds;

import java.awt.image.*;
import java.nio.*;
import java.util.*;

/* Resources:
 * http://www.matejtomcik.com/Public/KnowHow/DXTDecompression/
 * http://www.fsdeveloper.com/wiki/index.php?title=DXT_compression_explained
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
			
			int r8 = (int)(255.0f/31.0f * r5);
			int g8 = (int)(255.0f/63.0f * g5);
			int b8 = (int)(255.0f/31.0f * b5);
			
			return 0xFF000000 | r8 << 16 | g8 << 8 | b8;
		});
		
		map.put(DDSImage.D3DFMT_A1R5G5B5, (PixelTransform) (ByteBuffer bb) -> {
			short rawPixel = bb.getShort();
			
			int a8 = (rawPixel & 0x8000) == 0 ? 0 : 0xFF;
			
			int r5 = (rawPixel & 0x7C00) >>> 10;
			int g5 = (rawPixel & 0x03E0) >>> 5;
			int b5 = (rawPixel & 0x001F);
			
			int r8 = (int)(255.0f/31.0f * r5);
			int g8 = (int)(255.0f/31.0f * g5);
			int b8 = (int)(255.0f/31.0f * b5);
			
			return a8 << 24 | r8 << 16 | g8 << 8 | b8;
		});

		map.put(DDSImage.D3DFMT_X1R5G5B5, (PixelTransform) (ByteBuffer bb) -> {
			short rawPixel = bb.getShort();
			
			int a8 = 0xFF;
			
			int r5 = (rawPixel & 0x7C00) >>> 10;
			int g5 = (rawPixel & 0x03E0) >>> 5;
			int b5 = (rawPixel & 0x001F);
			
			int r8 = (int)(255.0f/31.0f * r5);
			int g8 = (int)(255.0f/31.0f * g5);
			int b8 = (int)(255.0f/31.0f * b5);
			
			return a8 << 24 | r8 << 16 | g8 << 8 | b8;
		});

		map.put(DDSImage.D3DFMT_R8G8B8, (PixelTransform) (ByteBuffer bb) -> {
			return _read_le24(bb) & 0x00FFFFFF | 0xFF000000;
		});

		map.put(DDSImage.D3DFMT_A8R8G8B8, (PixelTransform) (ByteBuffer bb) -> {
			return bb.getInt();
		});
		
		map.put(DDSImage.D3DFMT_X8R8G8B8, (PixelTransform) (ByteBuffer bb) -> {
			return 0xFF000000 | (bb.getInt() & 0x00FFFFFF);
		});
		
		map.put(DDSImage.D3DFMT_A8B8G8R8, (PixelTransform) (ByteBuffer bb) -> {
			int pixel = bb.getInt();
			return pixel & 0xFF00FF00 |
				((pixel & 0x00FF0000) >> 16) |
				((pixel & 0x000000FF) << 16);
		});
		
		map.put(DDSImage.D3DFMT_X8B8G8R8, (PixelTransform) (ByteBuffer bb) -> {
			int pixel = bb.getInt();
			return 0xFF000000 | pixel & 0x0000FF00 |
			((pixel & 0x00FF0000) >> 16) |
			((pixel & 0x000000FF) << 16);
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
		
		dds.debugPrint();
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
			case DDSImage.D3DFMT_DXT3:
				return _parseDXT(image, 3);
			case DDSImage.D3DFMT_DXT5:
				return _parseDXT(image, 5);
				
			/* No one likes these, so don't bother with them. */
			case DDSImage.D3DFMT_DXT2:
			case DDSImage.D3DFMT_DXT4:
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
				bimg.setRGB(i, j, transform.getPixel(rawData));
			}
		}
		
		return bimg;
	}

	private static class DXTBlock {
		public final int[] alphaTable;
		public final int[] alphaIndices;
		public final ARGBColour[] colours;

		public DXTBlock() {
			colours = new ARGBColour[4];
			for(int i = 0; i < colours.length; ++i)
				colours[i] = new ARGBColour();
			
			alphaTable = new int[16];
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

				applyTable(i, j, rawData.getInt(), block, bimg, version != 1);
			}
		}
		
		return bimg;
	}

	static int fuckdex[] = new int[] {
		 0,  1,  2,  3,
		 4,  5,  6,  7,
		 8,  9, 10, 11,
		12, 13, 14, 15
	};
	
	static int fuckdex2[] = new int[] {
		 3,  2,  1,  0,
		 7,  6,  5,  4,
		11, 10,  9,  8,
		15, 14, 13, 12
	};

	static int fuckdex2a[] = new int[] {
		 3,  2,  1,  0,
		 7,  6,  5,  4,
		11, 10,  9,  8,
		15, 14, 13, 12
	};
	
	static int fuckdex3[] = new int[] {
		 3,  2,  1,  0,
		 7,  6,  5,  4,
		11, 10,  9,  8,
		15, 14, 13, 12
	};

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
	 */
	private static void applyTable(int i, int j, int table, DXTBlock block, BufferedImage bimg, boolean useAlphaTable) {
		
		int mask = 0b11;
		
		for(int x = 0, k = 0; x < 4; ++x) for(int y = 0; y < 4; ++y, ++k) {
			int pixel = block.colours[(table & (mask << (2*k))) >>> (2*k)].get();
			
			if(useAlphaTable) {
				
				int alpha = 0xFF;//block.alphaTable[block.alphaIndices[fuckdex2a[k]]];
				pixel = pixel & 0x00FFFFFF | (alpha << 24);
			}
			
			bimg.setRGB(i*4 + y, j*4 + x, pixel);
		}
		
//		bimg.setRGB(i*4 + 0, j*4 + 0, (block.colours[(table & 0b00000000000000000000000000000011) >>>  0].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[ 0]] << 24);
//		bimg.setRGB(i*4 + 1, j*4 + 0, (block.colours[(table & 0b00000000000000000000000000001100) >>>  2].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[ 1]] << 24);
//		bimg.setRGB(i*4 + 2, j*4 + 0, (block.colours[(table & 0b00000000000000000000000000110000) >>>  4].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[ 2]] << 24);
//		bimg.setRGB(i*4 + 3, j*4 + 0, (block.colours[(table & 0b00000000000000000000000011000000) >>>  6].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[ 3]] << 24);
//
//		bimg.setRGB(i*4 + 0, j*4 + 1, (block.colours[(table & 0b00000000000000000000001100000000) >>>  8].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[ 4]] << 24);
//		bimg.setRGB(i*4 + 1, j*4 + 1, (block.colours[(table & 0b00000000000000000000110000000000) >>> 10].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[ 5]] << 24);
//		bimg.setRGB(i*4 + 2, j*4 + 1, (block.colours[(table & 0b00000000000000000011000000000000) >>> 12].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[ 6]] << 24);
//		bimg.setRGB(i*4 + 3, j*4 + 1, (block.colours[(table & 0b00000000000000001100000000000000) >>> 14].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[ 7]] << 24);
//
//		bimg.setRGB(i*4 + 0, j*4 + 2, (block.colours[(table & 0b00000000000000110000000000000000) >>> 16].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[ 8]] << 24);
//		bimg.setRGB(i*4 + 1, j*4 + 2, (block.colours[(table & 0b00000000000011000000000000000000) >>> 18].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[ 9]] << 24);
//		bimg.setRGB(i*4 + 2, j*4 + 2, (block.colours[(table & 0b00000000001100000000000000000000) >>> 20].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[10]] << 24);
//		bimg.setRGB(i*4 + 3, j*4 + 2, (block.colours[(table & 0b00000000110000000000000000000000) >>> 22].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[11]] << 24);
//
//		bimg.setRGB(i*4 + 0, j*4 + 3, (block.colours[(table & 0b00000011000000000000000000000000) >>> 24].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[12]] << 24);
//		bimg.setRGB(i*4 + 1, j*4 + 3, (block.colours[(table & 0b00001100000000000000000000000000) >>> 26].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[13]] << 24);
//		bimg.setRGB(i*4 + 2, j*4 + 3, (block.colours[(table & 0b00110000000000000000000000000000) >>> 28].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[14]] << 24);
//		bimg.setRGB(i*4 + 3, j*4 + 3, (block.colours[(table & 0b11000000000000000000000000000000) >>> 30].get() & 0x00FFFFFF) | block.alphaTable[block.alphaIndices[15]] << 24);
	}

	/**
	 * Parse a DXT2/3 alpha table into an array.
	 * @param alphas The array to write the alpha values into.
	 * @param rawTable The raw table.
	 */
	private static void _parseAlphaTable(int[] alphas, long rawTable) {
		
		long mask = 0b1111;
		
		for(int i = 0; i < 16; ++i) {
			int alpha4 = (int)((rawTable & (mask << (long)(4*i))) >>> (4*i));
			alphas[i] = (int)(255.0f/15.0f * alpha4);
		}
	}
	
	/**
	 * Calculate the 8 interpolated alpha values from the two at the start of
	 * the block.
	 * @param alphas The array to receive the alpha values.
	 * @param bb The buffer containing the data.
	 */
	private static void interpolateAlphas(int[] alphas, ByteBuffer bb) {
		alphas[0] = bb.get() & 0xFF;
		alphas[1] = bb.get() & 0xFF;
		if(alphas[0] > alphas[1]) {
			alphas[2] = (6 * alphas[0] + 1 * alphas[1]) / 7;
			alphas[3] = (5 * alphas[0] + 2 * alphas[1]) / 7;
			alphas[4] = (4 * alphas[0] + 3 * alphas[1]) / 7;
			alphas[5] = (3 * alphas[0] + 4 * alphas[1]) / 7;
			alphas[6] = (2 * alphas[0] + 5 * alphas[1]) / 7;
			alphas[7] = (1 * alphas[0] + 6 * alphas[1]) / 7;
		} else {
			alphas[2] = (4 * alphas[0] + 1 * alphas[1]) / 5;
			alphas[3] = (3 * alphas[0] + 2 * alphas[1]) / 5;
			alphas[4] = (2 * alphas[0] + 3 * alphas[1]) / 5;
			alphas[5] = (1 * alphas[0] + 4 * alphas[1]) / 5;
			alphas[6] = 0x00;
			alphas[7] = 0xFF;
		}
	}
	
	/**
	 * Build the alpha index table for a DXT4/5 image.
	 * @param idx The array to receive the indices.
	 * @param bb The buffer containing the index table.
	 */
	private static void buildIndexTable(int[] idx, ByteBuffer bb) {
		final byte[] raw = new byte[3];

		bb.get(raw);
		
		idx[ 0] = (raw[2] & 0b00000111);
		idx[ 1] = (raw[2] & 0b00111000) >>> 3;
		idx[ 2] = (raw[2] & 0b11000000) >>> 6 |
				  (raw[1] & 0b00000001) <<  2;
		
		idx[ 3] = (raw[1] & 0b00001110) >>> 1;
		idx[ 4] = (raw[1] & 0b01110000) >>> 4;
		idx[ 5] = (raw[1] & 0b10000000) >>> 7 |
				  (raw[0] & 0b00000011) <<  1;
		
		idx[ 6] = (raw[0] & 0b00011100) >>> 2;
		idx[ 7] = (raw[0] & 0b11100000) >>> 5;
		
		bb.get(raw);
		
		idx[ 8] = (raw[2] & 0b00000111);
		idx[ 9] = (raw[2] & 0b00111000) >>> 3;
		idx[10] = (raw[2] & 0b11000000) >>> 6 |
				  (raw[1] & 0b00000001) <<  2;
		
		idx[11] = (raw[1] & 0b00001110) >>> 1;
		idx[12] = (raw[1] & 0b01110000) >>> 4;
		idx[13] = (raw[1] & 0b10000000) >>> 7 |
				  (raw[0] & 0b00000011) <<  1;
		
		idx[14] = (raw[0] & 0b00011100) >>> 2;
		idx[15] = (raw[0] & 0b11100000) >>> 5;
	}

	private static void _calcDXTNot1ColComponent(DXTBlock block) {
		
		ARGBColour cols[] = block.colours;

		for(int i = 1; i <= 3; ++i) {
			cols[2].set(i, (2 * cols[0].get(i) + cols[1].get(i)) / 3);
			cols[3].set(i, (2 * cols[1].get(i) + cols[0].get(i)) / 3);
		}
		
		cols[0].setAlpha(0);
		cols[1].setAlpha(0);
		cols[2].setAlpha(0);
		cols[3].setAlpha(0);
	}
	
	private static void _calcDXT1ColComponent(DXTBlock block) {
		
		ARGBColour cols[] = block.colours;

		int col0ARGB = cols[0].get();
		int col1ARGB = cols[1].get();
		
		if(col0ARGB > col1ARGB)
			cols[3].setAlpha(0xFF);
		else
			cols[3].setAlpha(0);

		cols[0].setAlpha(0xFF);
		cols[1].setAlpha(0xFF);
		cols[2].setAlpha(0xFF);
		
		for(int i = 1; i <= 3; ++i) {
			if(col0ARGB > col1ARGB) {
				cols[2].set(i, (2 * cols[0].get(i) + cols[1].get(i)) / 3);
				cols[3].set(i, (2 * cols[1].get(i) + cols[0].get(i)) / 3);
			} else {
				cols[2].set(i, (cols[0].get(i) + cols[1].get(i)) / 2);
				cols[3].set(i, 0x00);
			}
		}
	}


	private interface PixelTransform {
		/**
		 * Read a pixel from the buffer and convert it to A8R8G8B8.
		 * @param bb The buffer containing the pixel.
		 * @return An A8R8G8B8 pixel.
		 */
		public int getPixel(ByteBuffer bb);
	}
}
