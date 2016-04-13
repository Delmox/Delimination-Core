package org.ddos.client;

import java.io.IOException;
import java.util.Scanner;

import org.ddos.network.ClientNetwork;
import org.jcom.Command;
import org.jcom.CommandData;
import org.jcom.CommandInterface;
import org.jcom.InvalidCommandArgumentsException;
import org.jcom.UnknownCommandException;
import org.jnetwork.Connection;

public class ClientMain {
	public static void main(String[] args) {
		try {
			ClientNetwork.setClient(new Connection("server2.jacobsrandomsite.com", 25565));
		} catch (IOException e) {
			e.printStackTrace();
		}

		ClientCommands commands = new ClientCommands();

		Command lastCommand = null;
		try (Scanner in = new Scanner(System.in)) {
			while (true) {
				System.out.print("Delimination>");
				try {
					commands.executeCommand(lastCommand = CommandInterface.parseCommand(in.nextLine()));
				} catch (InvalidCommandArgumentsException e) {
					System.out.println("Usage(s): ");
					try {
						for (CommandData data : commands.getCommandData(lastCommand.getBaseCommand())) {
							System.out.println("\t" + data.getUsage());
						}
					} catch (UnknownCommandException e1) {
						// never happens
					}
				} catch (UnknownCommandException e) {
					System.out.println("Unknown command: " + lastCommand.getBaseCommand());
					System.out.println(
							"Type \"help\" for a list of commands or \"help <command>\" for help with a specific command.");
				}
			}
		}
	}
}