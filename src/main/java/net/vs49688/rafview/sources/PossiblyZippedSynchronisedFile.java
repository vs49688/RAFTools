package net.vs49688.rafview.sources;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.*;

public class PossiblyZippedSynchronisedFile extends SynchronisedFile {

	public PossiblyZippedSynchronisedFile(Path path) throws IOException, IllegalArgumentException {
		super(path);
	}
	
	@Override
	protected byte[] decompress(byte[] data, int size) throws IOException {
		byte[] tmp = new byte[1024];
		Inflater inf = new Inflater();

		inf.setInput(data);
		
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream(size)) {	
			while(!inf.finished())
				bos.write(tmp, 0, inf.inflate(tmp));

			tmp = bos.toByteArray();
		} catch(DataFormatException e) {
			/* We're uncompressed, just return the raw data */
			return data;
		} catch(IOException e) {
			throw e;
		}
		
		inf.end();
		return tmp;
	}
}
