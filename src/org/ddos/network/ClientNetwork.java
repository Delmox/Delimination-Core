package org.ddos.network;

import java.io.IOException;

import org.jnetwork.Connection;
import org.jnetwork.DataPackage;

public class ClientNetwork {
	private static Connection client;

	public static void setClient(Connection client) throws IOException {
		ClientNetwork.client = client;
		client.getOutputStream().writeObject(new DataPackage(false).setMessage("INITIAL_PACKAGE"));
	}

	public static Connection getClient() {
		return client;
	}
}