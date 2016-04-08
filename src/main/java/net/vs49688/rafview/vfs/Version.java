/*
 * RAFTools - Copyright (C) 2015 Zane van Iperen.
 *    Contact: zane@zanevaniperen.com
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
import net.vs49688.rafview.sources.*;

public class Version {

	public Version(String v, DataSource ds) {
		version = v;
		dataSource = ds;
	}

	@Override
	public String toString() {
		return version;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Version)) {
			return false;
		}

		Version v = (Version) o;
		return version.equals(v.version);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + Objects.hashCode(version);
		return hash;
	}

	public boolean versionCompare(String version) {
		if(version == null) {
			return false;
		}
		
		return version.equalsIgnoreCase(version);
	}
	
	public final String version;
	public final DataSource dataSource;
}
