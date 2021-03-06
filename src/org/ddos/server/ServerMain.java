package org.ddos.server;

import java.io.IOException;

import org.jnetwork.Server;
import org.jnetwork.SocketType;

public class ServerMain {
	public static void main(String[] args) {
		DDOSServer server = new DDOSServer();

		System.out.println("Started server on port 25565.");
		try {
			server.setServer(new Server(SocketType.DEFAULT, 25565, server));
			server.getServer().addClientDisconnectionListener(server);
			server.getServer().start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			server.getServer().waitUntilClose();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Server closed.");
	}
}