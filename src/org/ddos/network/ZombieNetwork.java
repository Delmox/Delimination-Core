package org.ddos.network;

import java.io.IOException;

import org.jnetwork.Connection;
import org.jnetwork.DataPackage;

public class ZombieNetwork {
	private static Connection client;

	public static void setClient(Connection client) throws IOException {
		ZombieNetwork.client = client;
		client.getOutputStream().writeObject(new DataPackage(true).setMessage("INITIAL_PACKAGE"));
	}

	public static Connection getClient() {
		return client;
	}
}