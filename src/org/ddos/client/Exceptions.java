package org.ddos.client;

public class Exceptions {
	public static void throwException(Throwable e) {
		Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
	}

	public static void unconnectedException() {
		System.out
				.println("You aren't connected to a server. Type \"help connect\" for information on how to connect.");
	}
}