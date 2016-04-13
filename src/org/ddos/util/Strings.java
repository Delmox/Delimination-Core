package org.ddos.util;

public class Strings {
	public static String getString(int amount, char c) {
		return new String(new char[amount]).replace('\0', c);
	}
}