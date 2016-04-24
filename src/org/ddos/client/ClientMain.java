package org.ddos.client;

import java.io.IOException;

import org.ddos.client.command.ClientCommands;
import org.ddos.network.ClientNetwork;
import org.ddos.util.Console;
import org.ddos.util.ServerConstants;
import org.jcom.Command;
import org.jcom.CommandData;
import org.jcom.CommandInterruptedException;
import org.jcom.InvalidCommandArgumentsException;
import org.jcom.UnknownCommandException;
import org.jcom.UnknownFlagException;
import org.jnetwork.Connection;
import org.jnetwork.DataPackage;
import org.jnetwork.SocketType;

public class ClientMain {
	public static void main(String[] args) {
		System.out.println("Welcome to the Delimination client.");

		ClientCommands commands = new ClientCommands();
		try {
			ClientNetwork.setClient(new Connection(SocketType.DEFALT, ServerConstants.ADDRESS, ServerConstants.PORT));
		} catch (IOException e2) {
			System.out.println("The server is down. Plese try again later.");
			return;
		}

		while (true) {
			System.out.print("Enter the admin password: ");
			String password = Console.input.nextLine();
			try {
				ClientNetwork.getClient().getOutputStream()
						.writeObject(new DataPackage(false, password).setMessage("INITIAL_PACKAGE"));

				if (!(boolean) ClientNetwork.getClient().getInputStream().readObject()) {
					System.out.println("Invalid password.");
					System.exit(1);
				} else {
					break;
				}
			} catch (Exception e2) {
				System.out.println(
						"An exception occurred: " + e2.getMessage() + " (" + e2.getClass().getSimpleName() + ")");
				return;
			}
		}

		Command lastCommand = null;
		while (true) {
			System.out.print("Delimination>");
			String raw = Console.input.nextLine().trim();
			if (raw.isEmpty())
				continue;

			try {
				commands.executeCommand(lastCommand = commands.parseCommand(raw));
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