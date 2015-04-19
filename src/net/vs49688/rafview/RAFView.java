package net.vs49688.rafview;

import net.vs49688.rafview.vfs.RAFS;
import java.util.*;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RAFView {
	
	public static void main(String[] args) throws IOException {
		RAFS vfs = new RAFS();

		addAll(vfs, "F:\\Games\\League of Legends");
		vfs.dumpPaths();
		//vfs.dumpToDir("F:\\lolex");
		System.err.printf("Using %s bytes of memory\n", Runtime.getRuntime().totalMemory());
	}
	
	private static void addAll(RAFS vfs, String baseDir) throws IOException {
		/* Generate the path to "filearchives" */
		Path filearchives = Paths.get(baseDir, "RADS", "projects", "lol_game_client", "filearchives");
		
		/* List the files, taking only directories of the form X.X.X.X */
		DirectoryStream<Path> stream = Files.newDirectoryStream(filearchives, (Path entry) -> {
			if(!Files.isDirectory(entry))
				return false;
			
			String name = entry.getFileName().toString();
			
			String[] sOctets = name.split("\\.");
			if(sOctets.length != 4)
				return false;
			
			int octet;
			try {
				for(int i = 0; i < 4; ++i) {
					octet = Integer.parseInt(sOctets[i]);
					
					if(octet < 0 || octet > 255)
						return false;
				}
			} catch(Exception e) {
				return false;
			}
			
			return true;
		});
		
		/* Get the list of versions */
		ArrayList<String> versions = new ArrayList<>();
		for(final Path entry : stream)
			versions.add(entry.getFileName().toString());
		
		/* Sort them */
		versions.sort(new IPv4Sorter());
		
		/* Add them */
		for(final String v : versions)
			addVersion(vfs, filearchives, v);
	}
	
	public static void addVersion(RAFS vfs, Path filearchives, String version) throws IOException {
		Path versionPath = Paths.get(filearchives.toString(), version);
		
		/* List the files, taking only files of the form Archive_X.raf[.dat] */
		DirectoryStream<Path> stream = Files.newDirectoryStream(versionPath, (Path entry) -> {
			
			String name = entry.getFileName().toString().toLowerCase();
			
			if(!name.startsWith("archive_"))
				return false;
			
			if(!name.endsWith(".raf") && !name.endsWith(".raf.dat"))
				return false;

			return true;
		});
		
		ArrayList<Path> files = new ArrayList<>(2);
		for(final Path entry : stream)
			files.add(entry);
		
		if(files.size() % 2 != 0)
			throw new IOException(String.format("Mismatched .raf[.dat] pair in %s", versionPath));
		
		/* Put the .raf files first, and the .raf.dat files second */
		files.sort((Path p1, Path p2) -> { return p1.getFileName().compareTo(p2.getFileName());	});

		/* Add them */
		for(int i = 0; i < files.size(); i += 2) {
			vfs.addFile(files.get(i), files.get(i+1), version);
		}
	}
}
