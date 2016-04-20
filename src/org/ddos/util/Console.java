package org.ddos.util;

import java.util.Scanner;

public class Console {
	public static final Scanner input = new Scanner(System.in);

	public static void println(Object o) {
		System.out.println(o);
	}

	public static void println() {
		System.out.println();
	}

	public static void debug(Object o) {
		System.err.println(o);
	}

	public static void printArray(Object[] a) {
		for (Object o : a) {
			debug(o);
		}
	}
}