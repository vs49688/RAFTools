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
package net.vs49688.rafview.cli;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import net.vs49688.rafview.IPv4Sorter;
import net.vs49688.rafview.vfs.*;


public class Model {
	private static final Pattern s_RAFPattern = Pattern.compile("Archive_(\\d+)\\.raf(\\.dat|)");
	private final RAFS m_VFS;
	
	private Path m_CurrentDir;
	
	
	public Model() {
		m_VFS = new RAFS();
		m_CurrentDir = m_VFS.getRoot().getFullPath();
	}
	
	public void addFile(Path file, String version) throws IOException {
		Path dat = Paths.get(String.format("%s.dat", file.toString()));
		m_VFS.addFile(file, dat, version);
		
	}
	
	public void openLolDirectory(Path path) throws IOException {
		m_VFS.clear();
		addAll(m_VFS, path);
	}

	public Path getCurrentDirectory() {
		return m_CurrentDir;
	}
	
	public void setCurrentDirectory(Path path) {
		m_CurrentDir = path;
	}
	
	public RAFS getVFS() {
		return m_VFS;
	}
	
	private static void addAll(RAFS vfs, Path baseDir) throws IOException {
		/* Generate the path to "filearchives" */
		Path filearchives = Paths.get(baseDir.toString(), "RADS", "projects", "lol_game_client", "filearchives");
		
		if(!Files.exists(filearchives))
			throw new IOException("Not a valid LoL directory");
		
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
			} catch(NumberFormatException e) {
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
	
	private static void addVersion(RAFS vfs, Path filearchives, String version) throws IOException {
		Path versionPath = Paths.get(filearchives.toString(), version);
		
		/* List the files, taking only files of the form Archive_\\d+.raf[.dat] */
		DirectoryStream<Path> stream = Files.newDirectoryStream(versionPath, (Path entry) -> {
			return s_RAFPattern.matcher(entry.getFileName().toString()).find();
		});
		
		ArrayList<Path> files = new ArrayList<>(2);
		for(final Path entry : stream)
			files.add(entry);

		/* Ensure we have file pairs */
		if(!validatePairs(files))
			throw new IOException(String.format("Mismatched .raf[.dat] pair in %s", versionPath));

		/* Put the .raf files first, and the .raf.dat files second */
		files.sort((Path p1, Path p2) -> { return p1.getFileName().compareTo(p2.getFileName());	});

		/* Add them */
		/* BUGBUGBUG: If archives in the same folder have the same file, then
		   whichever one is loaded last will be the one in the VFS */
		for(int i = 0; i < files.size(); i += 2)
			vfs.addFile(files.get(i), files.get(i+1), version);
	}
	
	/**
	 * Ensure we have a valid .raf .raf.dat pair.
	 * @param files The list of files to validate.
	 * @return true if each .raf file is matched by a .raf.dat file, otherwise
	 * false.
	 */
	private static boolean validatePairs(List<Path> files) {

		/* If we're not even, don't even bother */
		if((files.size() & 1) != 0)
			return false;

		Map<Integer, Integer> pairs = new HashMap<>();
		
		/* Ensure we have pairs of files */
		for(final Path p: files) {
			Path f = p.getFileName();
			
			Matcher m = s_RAFPattern.matcher(f.toString());
			
			if(!m.find())
				return false;

			int id = Integer.parseInt(m.group(1));
			
			int kek;
			if(pairs.containsKey(id))
				kek = pairs.get(id);
			else
				kek = 0;
			
			/* 0b10 = .raf.dat, 0b01 = .raf */
			int flag = m.group(2).isEmpty() ? 0b01 : 0b10;

			/* Already have that file */
			if((kek & flag) != 0)
				return false;
			
			kek |= flag;

			pairs.put(id, kek);
		}
		
		return pairs.keySet().stream().noneMatch((id) -> ((pairs.get(id) & 0b11) != 0b11));
	}
	
	public static String getVersionString() {
		return "0.4.2-alpha-wwise";
	}
	
	public static String getApplicationName() {
		return "RAFTools";
	}
	
	public static String getContactEmail() {
		return "zane.vaniperen@uqconnect.edu.au";
	}
	
	public static String getCopyrightHolder() {
		return "Zane van Iperen";
	}
	
	public static int getCopyrightYear() {
		return 2015;
	}
}
