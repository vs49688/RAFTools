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
package net.vs49688.rafview;

import java.util.*;

public class IPv4Sorter implements Comparator<String> {
	/**
	 * Splits an IPv4 address into an array of octets.
	 * X must be in the range [0, 255]
	 * @param s The string to split.
	 * @return An array of 4 integers containing each octet.
	 */
	private static int[] getOctets(String s) {
		if(s == null)
			throw new IllegalArgumentException();

		String[] sOctets = s.split("\\.");
		if(sOctets.length != 4)
			throw new IllegalArgumentException();

		int[] octets = new int[4];

		for(int i = 0; i < 4; ++i) {
			octets[i] = Integer.parseInt(sOctets[i]);

			if(octets[i] < 0 || octets[i] > 255)
				throw new IllegalArgumentException();
		}

		return octets;
	}

	@Override
	public int compare(String s1, String s2) {
		return _compare(s1, s2);
	}
	
	
	public static int _compare(String s1, String s2) {
		int[] octet1 = getOctets(s1);
		int[] octet2 = getOctets(s2);

		for(int i = 0; i < 4; ++i) {
			if(octet1[i] < octet2[i])
				return -1;
			else if(octet1[i] > octet2[i])
				return 1;
		}

		return 0;
	}
}
