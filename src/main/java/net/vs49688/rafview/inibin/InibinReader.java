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
package net.vs49688.rafview.inibin;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.text.ParseException;

public class InibinReader {

	private static final int FLAG_UNK1		= 0b0000000000000001;
	private static final int FLAG_UNK2		= 0b0000000000000010;
	private static final int FLAG_IDIV10	= 0b0000000000000100;	// Integer divided by 10
	private static final int FLAG_SHORT		= 0b0000000000001000;	// 2-byte integers
	private static final int FLAG_BYTE		= 0b0000000000010000;	// 1-byte integers
	private static final int FLAG_BITFIELD	= 0b0000000000100000;	// 1-byte packed booleans
	private static final int FLAG_UNK7		= 0b0000000001000000;	// RGB colour (1 byte * 3 reads)?
	private static final int FLAG_POSITION	= 0b0000000010000000;	// Position (1 float * 3 reads)
	private static final int FLAG_UNK9		= 0b0000000100000000;	// Something (.troybin only) (2 byte blocks)
	private static final int FLAG_UNK10		= 0b0000001000000000;	// Something (.troybin only) (8 byte blocks (double? long?))
	private static final int FLAG_RGBA		= 0b0000010000000000;	// RGBA colour (1 byte * 4 reads)?
	private static final int FLAG_UNK12		= 0b0000100000000000;	// Something (.troybin only) (16 byte blocks, 4 floats?)
																	// Definately NOT 2 doubles, 4 ints, or 2 longs.
	private static final int FLAG_SOFFSETS	= 0b0001000000000000;	// String table offsets
	private static final int FLAG_UNK13		= 0b0010000000000000;
	private static final int FLAG_UNK14		= 0b0100000000000000;
	private static final int FLAG_UNK15		= 0b1000000000000000;
	
	private static final int FLAGS_KNOWN	= FLAG_UNK1 | FLAG_UNK2 |
			FLAG_IDIV10 | FLAG_SHORT | FLAG_BYTE | FLAG_BITFIELD | FLAG_UNK7 |
			FLAG_POSITION | FLAG_UNK9 | FLAG_UNK10 | FLAG_RGBA | FLAG_UNK12 |
			FLAG_SOFFSETS;
	
	private static final Map<Integer, FlagHandler> m_FlagHandlers = _initHandlers();
	
	/**
	 * Initialise the handlers for each "flag"
	 * @return The mapping for each flag to its handler.
	 */
	private static Map<Integer, FlagHandler> _initHandlers() {
		HashMap<Integer, FlagHandler> ret = new HashMap<>();
		
		ret.put(FLAG_UNK1, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parseUnk1(map, buffer);
		});
		
		ret.put(FLAG_UNK2, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parseUnk2(map, buffer);
		});
		
		ret.put(FLAG_IDIV10, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parseIDiv10(map, buffer);
		});
		
		ret.put(FLAG_SHORT, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parseShort(map, buffer);
		});
		
		ret.put(FLAG_BYTE, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parseByte(map, buffer);
		});
		
		ret.put(FLAG_BITFIELD, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parseBitfield(map, buffer);
		});
		
		ret.put(FLAG_UNK7, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parseUnk7(map, buffer);
		});
	
		ret.put(FLAG_POSITION, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parsePosition(map, buffer);
		});

		ret.put(FLAG_UNK9, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parseUnk9(map, buffer);
		});
		
		ret.put(FLAG_UNK10, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parseUnk10(map, buffer);
		});

		ret.put(FLAG_RGBA, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parsePossibleRGBA(map, buffer);
		});

		ret.put(FLAG_UNK12, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parseUnk12(map, buffer);
		});

		ret.put(FLAG_SOFFSETS, (FlagHandler) (Map<Integer, Value> map, ByteBuffer buffer, int stLen) -> {
			_parseStringOffsets(map, buffer, stLen);
		});

		return ret;
	}

	public static Map<Integer, Value> readInibin(Path path) throws IOException, ParseException {
		
		try(FileInputStream rfis = new FileInputStream(path.toFile())) {
			MappedByteBuffer buffer;
			
			/* Map the file into memory */
			try (FileChannel fChannel = rfis.getChannel()) {
				buffer = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size());
			}

			return _readInibin(buffer);
		}
	}
	
	public static Map<Integer, Value> readInibin(byte[] data) throws IOException, ParseException {
		return _readInibin(ByteBuffer.wrap(data));
	}
	
	private static Map<Integer, Value> _readInibin(ByteBuffer buffer) throws IOException, ParseException {
		Map<Integer, Value> map = new HashMap<>();
		
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		/* First byte is the version */
		int version = buffer.get() & 0xF;

		System.err.printf("Version %d\n", version);
		if(version == 1) {
			parseV1(buffer);
		} else if(version == 2) {
			parseV2(map, buffer);
		} else {
			throw new ParseException(String.format("Invalid .inibin version %d", version), -1);
		}
		
		return map;
	}
	
	private static void parseV1(ByteBuffer buffer) throws IOException, ParseException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Parse a version 2 .inibin file.
	 * @param map The map the output values will be written to.
	 * @param buffer The input buffer.
	 * @throws IOException If an I/O error occurs.
	 * @throws ParseException If the format of the file is invalid.
	 */
	private static void parseV2(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		
		/* Read the length of the string table */
		int stLen = readUnsignedShort(buffer);
		
		/* Read the flags */
		int flags = readUnsignedShort(buffer);
		
		//System.err.printf("String Table Length: %d\n", stLen);
		dumpFlags(flags);
		
		/* Check if there are any unknown flags set */
		if((flags & (~FLAGS_KNOWN)) != 0) {
			throw new ParseException("Unknown flag set", -1);
		}
		
		/* Call each flag handler */
		for(int i = 0; i < 16; ++i) {
			if((flags & (1 << i)) != 0 && m_FlagHandlers.get(1 << i) != null)
				m_FlagHandlers.get(1 << i).parse(map, buffer, stLen);
		}
	}
	
	/**
	 * Read an unsigned short from a buffer.
	 * This is needed because Java doesn't have unsigned types.
	 * @param b The buffer to read from.
	 * @return An unsigned short from the buffer.
	 */
	private static int readUnsignedShort(ByteBuffer b) {
		return b.getShort() & 0xFFFF;
	}
	
	/**
	 * Read the keys for the next section.
	 * @param buffer The buffer to read from.
	 * @return An array of integers containing the read keys.
	 * @throws IOException If an I/O error occurred.
	 */
	private static int[] _readKeys(ByteBuffer buffer) throws IOException {
		int count = readUnsignedShort(buffer);
		
		int[] keys = new int[count];
		
		for(int i = 0; i < count; ++i)
			keys[i] = buffer.getInt();
		
		return keys;
	}
	
	
	private static void _parseUnk1(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		int[] keys = _readKeys(buffer);
		
		for(int i = 0; i < keys.length; ++i) {
			map.put(keys[i], new Value(buffer.getInt()));
		}
	}
	
	private static void _parseUnk2(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		int[] keys = _readKeys(buffer);
		
		for(int i = 0; i < keys.length; ++i) {
			map.put(keys[i], new Value(buffer.getFloat()));
		}
	}
	
	private static void _parseIDiv10(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		int[] keys = _readKeys(buffer);

		for(int i = 0; i < keys.length; ++i) {
			map.put(keys[i], new Value(((float)buffer.get())/10));
		}
	}
	
	private static void _parseShort(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		int[] keys = _readKeys(buffer);
		
		for(int i = 0; i < keys.length; ++i) {
			map.put(keys[i], new Value(buffer.getShort()));
		}
	}
	
	private static void _parseByte(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		int[] keys = _readKeys(buffer);
		
		for(int i = 0; i < keys.length; ++i) {
			map.put(keys[i], new Value(buffer.get()));
		}
	}
	
	private static void _parseBitfield(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		
		int[] keys = _readKeys(buffer);
		
		boolean[] kek = null;
		
		for(int i = 0; i < keys.length; ++i) {

			if(i % 8 == 0)
				kek = unpackBooleans(buffer.get());
			
			map.put(keys[i], new Value(kek[7 - (i % 8)]));
		}
	}
	
	private static void _parseUnk7(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		int[] keys = _readKeys(buffer);

		for(int i = 0; i < keys.length; ++i) {
			ArrayList<Value> tmp = new ArrayList<>(3);
			
			for(int j = 0; j < 3; ++j)
				tmp.add(new Value(((int)buffer.get()) & 0xFF));

			map.put(keys[i], new Value(tmp));
		}
	}

	private static void _parsePosition(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		int[] keys = _readKeys(buffer);
		
		for(int i = 0; i < keys.length; ++i) {
			map.put(keys[i], new Value(new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat())));
		}
	}
	
	private static void _parseUnk9(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		int[] keys = _readKeys(buffer);
		
		//Map<Integer, Value> fuck = new HashMap<>();
		for(int i = 0; i < keys.length; ++i) {
			List<Value> tmp = new ArrayList<>();
			for(int j = 0; j < 2; ++j) {
				tmp.add(new Value(buffer.get() & 0xFF));
			}

			map.put(keys[i], new Value(tmp));
			//fuck.put(keys[i], new Value(tmp));
		}
		
		//printMap(fuck);
		//System.exit(1);
		
	}
	
	private static void _parseUnk10(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		int[] keys = _readKeys(buffer);

		for(int i = 0; i < keys.length; ++i) {
			List<Value> tmp = new ArrayList<>();
			for(int j = 0; j < 8; ++j) {
				tmp.add(new Value(buffer.get() & 0xFF));
			}

			map.put(keys[i], new Value(tmp));
		}
	}
	
	private static void _parsePossibleRGBA(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		int[] keys = _readKeys(buffer);
		
		for(int i = 0; i < keys.length; ++i) {
			List<Value> tmp = new ArrayList<>();
			for(int j = 0; j < 4; ++j) {
				tmp.add(new Value(buffer.get() & 0xFF));
			}
			
			map.put(keys[i], new Value(tmp));
		}
	}

	private static void _parseUnk12(Map<Integer, Value> map, ByteBuffer buffer) throws IOException, ParseException {
		int[] keys = _readKeys(buffer);

		Map<Integer, Value> fuck = new HashMap<>();
		for(int i = 0; i < keys.length; ++i) {
			List<Value> tmp = new ArrayList<>();
			
			for(int j = 0; j < 4; ++j) {
				tmp.add(new Value(buffer.getFloat()));
			}
//			for(int j = 0; j < 16; ++j) {
//				tmp.add(new Value(buffer.get() & 0xFF));
//			}

			map.put(keys[i], new Value(tmp));
			fuck.put(keys[i], new Value(tmp));
		}
	}

	private static void _parseStringOffsets(Map<Integer, Value> map, ByteBuffer buffer, int stLen) throws IOException, ParseException {
		int[] keys = _readKeys(buffer);
		
		if(keys.length == 0)
			throw new IOException("_PSO: no keys");
		int[] offsets = new int[keys.length];
		for(int i = 0; i < keys.length; ++i) {
			offsets[i] = readUnsignedShort(buffer);
		}
		
		byte[] stringTable = new byte[stLen];
		buffer.get(stringTable);
		Map<Integer, Value> fuck = new HashMap<>();
		for(int i = 0; i < keys.length; ++i) {	
			map.put(keys[i], new Value(scanForString(stringTable, offsets[i])));
			fuck.put(keys[i], new Value(scanForString(stringTable, offsets[i])));
		}
		
		int x = 0;
	}

	/**
	 * Scan a byte buffer for a US-ASCII-encoded string.
	 * @param buffer The buffer to scan.
	 * @param start The position in the buffer to start scanning.
	 * @return The scanned string.
	 */
	private static String scanForString(byte[] buffer, int start) {
		StringBuilder sb = new StringBuilder();
		
		if(start >= buffer.length || start < 0)
			throw new IllegalArgumentException("start out of range");

		while(buffer[start] != 0)
			sb.append((char)buffer[start++]);

		return sb.toString();
	}
	
	/**
	 * Unpack a bitfield into a boolean array.
	 * @param b The bitfield.
	 * @return The boolean array.
	 */
	private static boolean[] unpackBooleans(byte b) {
		boolean[] ret = new boolean[8];
		
		for(int i = 0; i < 8; ++i) {
			ret[i] = (b & (1 << i)) != 0;
		}
		
		return ret;
	}

	private interface FlagHandler {
		public void parse(Map<Integer, Value> map, ByteBuffer buffer, int stLen) throws IOException, ParseException;
	}
	
	
	
	
	
	
	
	private static void dumpFlags(int flags) {
		System.err.printf("Flags: 0b%s\n", binaryString(flags, 16));
		System.err.printf("Unknown Flags: 0b%s\n", binaryString(flags & ~(FLAGS_KNOWN), 16));
		
		if((flags & FLAG_UNK1) != 0)			System.err.printf("FLAG_UNK1 ");
		if((flags & FLAG_UNK2) != 0)			System.err.printf("FLAG_UNK2 ");
		if((flags & FLAG_IDIV10) != 0)		System.err.printf("FLAG_IDIV10 ");
		if((flags & FLAG_SHORT) != 0)		System.err.printf("FLAG_SHORT ");
		if((flags & FLAG_BYTE) != 0)			System.err.printf("FLAG_BYTE ");
		if((flags & FLAG_BITFIELD) != 0)		System.err.printf("FLAG_BITFIELD ");
		if((flags & FLAG_UNK7) != 0)		System.err.printf("FLAG_UNK7 ");
		if((flags & FLAG_POSITION) != 0)	System.err.printf("FLAG_POSITION ");
		if((flags & FLAG_UNK9) != 0)			System.err.printf("FLAG_UNK9 ");
		if((flags & FLAG_UNK10) != 0)		System.err.printf("FLAG_UNK10 ");
		if((flags & FLAG_RGBA) != 0)			System.err.printf("FLAG_UNK11 ");
		if((flags & FLAG_UNK12) != 0)		System.err.printf("FLAG_UNK12 ");
		if((flags & FLAG_SOFFSETS) != 0)		System.err.printf("FLAG_SOFFSETS ");
		if((flags & FLAG_UNK13) != 0)		System.err.printf("FLAG_UNK13 ");
		if((flags & FLAG_UNK14) != 0)		System.err.printf("FLAG_UNK14 ");
		if((flags & FLAG_UNK15) != 0)		System.err.printf("FLAG_UNK15 ");
		
		System.err.printf("\n");
	}	
	
	private static String binaryString(final int number, final int binaryDigits) {
        final String response = String.format("%s%s",
				String.format(String.format("%%0%dd", binaryDigits), 0),
				Integer.toBinaryString(number));

        return response.substring(response.length() - binaryDigits);
    }
	
	/**
	 * Print the map to a Python dictionary ^_^
	 * @param map Teh mapz
	 */
	private static void printPythonDict(Map<Integer, Value> map) {
		System.out.print("{");
		int i = 0;
		for(final Integer key : map.keySet()) {
			
			if(i != 0)
				System.out.printf(" ");
			System.out.printf("%-12d: ", key);
			
			Value val = map.get(key);
			
			switch(val.getType()) {
				case BOOLEAN:
				case STRING:
					System.out.printf("'");
					break;
			}
			System.out.printf("%s", val.toString());
			
			switch(val.getType()) {
				case BOOLEAN:
				case STRING:
					System.out.printf("'");
					break;
			}
			
			
			if(i == map.size() - 1)
				System.out.printf("}");
			System.out.printf("\n");
			++i;
		}
	}
	
	private static void printMap(Map<Integer, Value> map) {
		for(final Integer key : map.keySet()) {
			Value val = map.get(key);
			System.out.printf("%12d => %8s:%s\n", key, val.getType(), val.toString());
		}
	}
}
