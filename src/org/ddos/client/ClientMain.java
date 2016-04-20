package org.ddos.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.ddos.client.command.ClientCommands;
import org.ddos.network.ClientNetwork;
import org.ddos.util.Console;
import org.ddos.util.ServerConstants;
import org.jcom.Command;
import org.jcom.CommandData;
import org.jcom.CommandInterface;
import org.jcom.CommandInterruptedException;
import org.jcom.InvalidCommandArgumentsException;
import org.jcom.UnknownCommandException;
import org.jcom.UnknownFlagException;
import org.jnetwork.CloseRequest;
import org.jnetwork.Connection;

public class ClientMain {
	public static void main(String[] args) {
		Socket socket = new Socket();
		CloseRequest.addObjectToClose(socket);

		try {
			socket.connect(new InetSocketAddress(ServerConstants.ADDRESS, ServerConstants.PORT), 1000);
		} catch (SocketTimeoutException e2) {
			System.err.println("The server is down, please launch the client when it is back up.");
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			ClientNetwork.setClient(new Connection(socket));
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Welcome to the Delimination client.");

		ClientCommands commands = new ClientCommands();

		Command lastCommand = null;
		while (true) {
			System.out.print("Delimination>");
			try {
				commands.executeCommand(lastCommand = CommandInterface.parseCommand(Console.input.nextLine()));
			} catch (InvalidCommandArgumentsException e) {
				System.out.println("Usage(s): ");
				try {
					for (CommandData data : commands.getCommandData(lastCommand.getBaseCommand())) {
						System.out.println("\t" + data.getUsage());
					}
				} catch (UnknownCommandException e1) {
					// never happens
				}
				System.out.println("Type \"help " + lastCommand.getBaseCommand() + "\" for help with the command.");
			} catch (UnknownCommandException e) {
				System.out.println("Unknown command: " + lastCommand.getBaseCommand());
				System.out.println(
						"Type \"help\" for a list of commands or \"help <command>\" for help with a specific command.");
			} catch (CommandInterruptedException e) {
				// who cares?
			} catch (UnknownFlagException e) {
				e.printStackTrace();
			}
		}
	}
}