package net.vs49688.rafview.dds;

import java.awt.image.*;
import java.nio.*;
import java.util.*;

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
				return _parseUncompressed(image, m_Transforms.get(pixelFormat));

			case DDSImage.D3DFMT_DXT1:
				return _parseDXT(image, 1);
			case DDSImage.D3DFMT_DXT5:
				return _parseDXT(image, 5);
		}
		return null;
	}
	
	/** Read a 24-bit, little-endian integer. */
	private static int _read_le24(ByteBuffer bb) {
		return (bb.get() & 0xFF) | ((bb.get() & 0xFF) << 8) | ((bb.get() & 0xFF) << 16);
	}
	
	private static BufferedImage _parseUncompressed(DDSImage.ImageInfo image, PixelTransform transform) {
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
		public final ARGBColour[] colours;

		public DXTBlock() {
			colours = new ARGBColour[4];
			for(int i = 0; i < colours.length; ++i)
				colours[i] = new ARGBColour();
		}
		
		// I hate this, but Java has no unions */
		public class ARGBColour {
			private int a, r, g, b;

			void set(int argb) {
				a = (argb & 0xFF000000) >>> 24;
				r = (argb & 0x00FF0000) >>> 16;
				g = (argb & 0x0000FF00) >>> 8;
				b = (argb & 0x000000FF);
			}
			
			void setAlpha(int alpha) { a = alpha & 0xFF; }
			void setRed(int red) { r = red & 0xFF; }
			void setGreen(int green) { g = green & 0xFF; }
			void setBlue(int blue) { b = blue & 0xFF; }
			
			int getAlpha() { return a; }
			int getRed() { return r; }
			int getGreen() { return g; }
			int getBlue() { return b; }
			
			int get() {
				return (a << 24) | (r << 16) | (g << 16) | b;
			}
		}
	}
	
	private static BufferedImage _parseDXT(DDSImage.ImageInfo image, int version) {
		BufferedImage bimg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		PixelTransform rgb565 = m_Transforms.get(DDSImage.D3DFMT_R5G6B5);
		
		ByteBuffer rawData = image.getData();
		
		DXTBlock block = new DXTBlock();
		
		for(int j = 0; j < image.getHeight() - 4; j += 4) {
			for(int i = 0; i < image.getWidth() - 4; i += 4) {
				block.colours[0].set(rgb565.getPixel(rawData));
				block.colours[1].set(rgb565.getPixel(rawData));

				_calcDXT1ColComponent(block, 1); // Red
				_calcDXT1ColComponent(block, 2); // Green
				_calcDXT1ColComponent(block, 3); // Blue

				block.colours[0].setAlpha(0xFF);
				block.colours[1].setAlpha(0xFF);
				block.colours[2].setAlpha(0xFF);
				block.colours[3].setAlpha(0xFF);
				
				int table = rawData.getInt();
				bimg.setRGB(i + 0, j + 0, block.colours[(table & 0b11000000000000000000000000000000) >>> 30].get());
				bimg.setRGB(i + 1, j + 0, block.colours[(table & 0b00110000000000000000000000000000) >>> 28].get());
				bimg.setRGB(i + 2, j + 0, block.colours[(table & 0b00001100000000000000000000000000) >>> 26].get());
				bimg.setRGB(i + 3, j + 0, block.colours[(table & 0b00000011000000000000000000000000) >>> 24].get());
				
				bimg.setRGB(i + 0, j + 1, block.colours[(table & 0b00000000110000000000000000000000) >>> 22].get());
				bimg.setRGB(i + 1, j + 1, block.colours[(table & 0b00000000001100000000000000000000) >>> 20].get());
				bimg.setRGB(i + 2, j + 1, block.colours[(table & 0b00000000000011000000000000000000) >>> 18].get());
				bimg.setRGB(i + 3, j + 1, block.colours[(table & 0b00000000000000110000000000000000) >>> 16].get());
				
				bimg.setRGB(i + 0, j + 2, block.colours[(table & 0b00000000000000001100000000000000) >>> 14].get());
				bimg.setRGB(i + 1, j + 2, block.colours[(table & 0b00000000000000000011000000000000) >>> 12].get());
				bimg.setRGB(i + 2, j + 2, block.colours[(table & 0b00000000000000000000110000000000) >>> 10].get());
				bimg.setRGB(i + 3, j + 2, block.colours[(table & 0b00000000000000000000001100000000) >>>  8].get());
				
				bimg.setRGB(i + 0, j + 3, block.colours[(table & 0b00000000000000000000000011000000) >>>  6].get());
				bimg.setRGB(i + 1, j + 3, block.colours[(table & 0b00000000000000000000000000110000) >>>  4].get());
				bimg.setRGB(i + 2, j + 3, block.colours[(table & 0b00000000000000000000000000001100) >>>  2].get());
				bimg.setRGB(i + 3, j + 3, block.colours[(table & 0b00000000000000000000000000000011) >>>  0].get());
				
				
				//System.err.printf("0x%X\n", table);
				//int x = 0;
			}
		}
		
		return bimg;
	}

	private static void _calcDXT1ColComponent(DXTBlock block, int index){
		
		DXTBlock.ARGBColour cols[] = block.colours;
	
		int col0, col1, col2, col3;
		if(index == 0) {
			col0 = cols[0].getAlpha();
			col1 = cols[1].getAlpha();
		} else if(index == 1) {
			col0 = cols[0].getRed();
			col1 = cols[1].getRed();
		} else if(index == 2) {
			col0 = cols[0].getGreen();
			col1 = cols[1].getGreen();
		} else if(index == 3) {
			col0 = cols[0].getBlue();
			col1 = cols[1].getBlue();
		} else {
			throw new IllegalArgumentException("index < 0 || index > 3");
		}
		
		if(col0 > col1) {
			col2 = (2 * col0 + col1) / 3;
			col3 = (2 * col1 + col0) / 3;
		} else {
			col2 = (col0 + col1) / 2;
			col3 = 0;
		}
		
		if(index == 0) {
			cols[2].setAlpha(col2);
			cols[3].setAlpha(col3);
		} else if(index == 1) {
			cols[2].setRed(col2);
			cols[3].setRed(col3);
		} else if(index == 2) {
			cols[2].setGreen(col2);
			cols[3].setGreen(col3);
		} else if(index == 3) {
			cols[2].setBlue(col2);
			cols[3].setBlue(col3);
		}
	}
	
//	private static void _calcDXT2345Colour(DXTBlock block) {
//		int cols[] = block.colours;
//		
//		cols[2] = (2 * cols[0] + cols[1]) / 3;
//		cols[3] = (2 * cols[1] + cols[0]) / 3;
//	}

	private interface PixelTransform {
		public int getPixel(ByteBuffer bb);
	}
}
