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
