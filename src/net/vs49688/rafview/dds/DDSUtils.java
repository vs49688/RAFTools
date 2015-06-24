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
	}
	
	private static BufferedImage _parseDXT(DDSImage.ImageInfo image, int version) {
		BufferedImage bimg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		PixelTransform rgb565 = m_Transforms.get(DDSImage.D3DFMT_R5G6B5);
		
		ByteBuffer rawData = image.getData();
		
		rawData.order(ByteOrder.LITTLE_ENDIAN);
		
		DXTBlock block = new DXTBlock();
		
		for(int j = 0; j < image.getHeight()/4; ++j) {
			for(int i = 0; i < image.getWidth()/4; ++i) {
				block.colours[0].set(rgb565.getPixel(rawData));
				block.colours[1].set(rgb565.getPixel(rawData));

				_calcDXT1ColComponent(block);
				int table = rawData.getInt();

				_fuck2(i, j, table, block, bimg);
			}
		}
		
		return bimg;
	}

	private static void _fuck2(int i, int j, int table, DXTBlock block, BufferedImage bimg) {
		bimg.setRGB(i*4 + 0, j*4 + 0, block.colours[(table & 0b00000000000000000000000000000011) >>>  0].get());
		bimg.setRGB(i*4 + 1, j*4 + 0, block.colours[(table & 0b00000000000000000000000000001100) >>>  2].get());
		bimg.setRGB(i*4 + 2, j*4 + 0, block.colours[(table & 0b00000000000000000000000000110000) >>>  4].get());
		bimg.setRGB(i*4 + 3, j*4 + 0, block.colours[(table & 0b00000000000000000000000011000000) >>>  6].get());

		bimg.setRGB(i*4 + 0, j*4 + 1, block.colours[(table & 0b00000000000000000000001100000000) >>>  8].get());
		bimg.setRGB(i*4 + 1, j*4 + 1, block.colours[(table & 0b00000000000000000000110000000000) >>> 10].get());
		bimg.setRGB(i*4 + 2, j*4 + 1, block.colours[(table & 0b00000000000000000011000000000000) >>> 12].get());
		bimg.setRGB(i*4 + 3, j*4 + 1, block.colours[(table & 0b00000000000000001100000000000000) >>> 14].get());

		bimg.setRGB(i*4 + 0, j*4 + 2, block.colours[(table & 0b00000000000000110000000000000000) >>> 16].get());
		bimg.setRGB(i*4 + 1, j*4 + 2, block.colours[(table & 0b00000000000011000000000000000000) >>> 18].get());
		bimg.setRGB(i*4 + 2, j*4 + 2, block.colours[(table & 0b00000000001100000000000000000000) >>> 20].get());
		bimg.setRGB(i*4 + 3, j*4 + 2, block.colours[(table & 0b00000000110000000000000000000000) >>> 22].get());

		bimg.setRGB(i*4 + 0, j*4 + 3, block.colours[(table & 0b00000011000000000000000000000000) >>> 24].get());
		bimg.setRGB(i*4 + 1, j*4 + 3, block.colours[(table & 0b00001100000000000000000000000000) >>> 26].get());
		bimg.setRGB(i*4 + 2, j*4 + 3, block.colours[(table & 0b00110000000000000000000000000000) >>> 28].get());
		bimg.setRGB(i*4 + 3, j*4 + 3, block.colours[(table & 0b11000000000000000000000000000000) >>> 30].get());
				
	}

	private static void _calcDXT1ColComponent(DXTBlock block){
		
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
