package org.ddos.network;

import java.io.IOException;

import org.jnetwork.Connection;

public class ClientNetwork {
	private static Connection client;

	public static void setClient(Connection client) throws IOException {
		ClientNetwork.client = client;
	}

	public static Connection getClient() {
		return client;
	}
}