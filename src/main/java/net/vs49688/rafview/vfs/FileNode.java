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
package net.vs49688.rafview.vfs;

import java.util.*;
import net.vs49688.rafview.IPv4Sorter;
import net.vs49688.rafview.sources.*;

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
	
    public FileNode(IOperationsNotify notify) {
        super(notify);
        m_Versions = new TreeSet<>(new VSort());
    }
	
	public FileNode(String name, IOperationsNotify notify) {
		super(name, notify);
		m_Versions = new TreeSet<>(new VSort());
	}

	public void addVersion(String version, DataSource ds) {
		if(version == null || version.isEmpty() || ds == null)
			throw new IllegalArgumentException();
		
		Version v = new Version(version, ds);
		
		if(m_Versions.contains(v)) {
			throw new IllegalArgumentException(String.format("Duplicate version %s for %s\n", version, getFullPath()));
		}
		
		m_Versions.add(v);
	}
	
	public Version getLatestVersion() {
		return m_Versions.last();
	}
	
	public Set<Version> getVersions() {
		return Collections.unmodifiableSet(m_Versions);
	}
	
	@Override
	public boolean isLeaf() {
		return true;
	}
	
	@Override
	public synchronized void _delete() {
		m_Versions.stream().forEach((v) -> {
			v.getSource().close();
		});
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
		
		private final String m_Version;
        private final DataSource m_DataSource;
    }
}
