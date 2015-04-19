package net.vs49688.rafview.vfs;

import java.io.IOException;
import java.util.*;
import net.vs49688.rafview.sources.DataSource;
import net.vs49688.rafview.IPv4Sorter;

public class FileNode extends Node {

    private final TreeSet<Version> m_Versions;

	private static class VSort implements Comparator<Version> {
		private final IPv4Sorter m_Sorter;
		
		public VSort() {
			m_Sorter = new IPv4Sorter();
		}
		
		@Override
		public int compare(Version v1, Version v2) {
			return m_Sorter.compare(v1.m_Version, v2.m_Version);
		}
	}
	
    public FileNode() {
        super();
        
        m_Versions = new TreeSet<>(new VSort());
    }
	
	public FileNode(String name) {
		super(name);
		
		m_Versions = new TreeSet<>(new VSort());
	}

	public void addVersion(String version, DataSource ds) {
		if(version == null || version.isEmpty() || ds == null)
			throw new IllegalArgumentException();
		
		Version v = new Version(version, ds);
		
		if(m_Versions.contains(v)) {
			throw new IllegalArgumentException();
		}
		
		m_Versions.add(v);
		
	}
	
	public Version getLatestVersion() {
		return m_Versions.last();
	}
	
	public Set<Version> getVersions() {
		return Collections.unmodifiableSet(m_Versions);
	}
	
    public static class Version {
		private Version(String v, DataSource ds) {
			m_Version = v;
			m_DataSource = ds;
		}
		
		public DataSource getSource() {
			return m_DataSource;
		}
		
		@Override
		public String toString() {
			return m_Version;
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof Version))
				return false;
			
			Version v = (Version)o;
			return m_Version.equals(v.m_Version);
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 97 * hash + Objects.hashCode(m_Version);
			return hash;
		}
		
		private String m_Version;
        private DataSource m_DataSource;
    }
}
