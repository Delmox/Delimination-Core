package org.ddos.client;

import java.util.regex.Pattern;

import org.ddos.client.command.ClientCommands;
import org.ddos.network.ClientNetwork;
import org.ddos.util.Console;
import org.jcom.Command;
import org.jcom.CommandData;
import org.jcom.CommandInterface;
import org.jcom.CommandInterruptedException;
import org.jcom.InvalidCommandArgumentsException;
import org.jcom.UnknownCommandException;
import org.jcom.UnknownFlagException;
import org.jnetwork.Connection;

public class ClientMain {
	public static void main(String[] args) {
		System.out.println("Welcome to the Delimination client.");

		ClientCommands commands = new ClientCommands();

		Command lastCommand = null;
		while (true) {
			try {
				System.out.print("Please connect to the server: ");
				String[] split = Console.input.nextLine().split(Pattern.quote(":"));
				ClientNetwork.setClient(new Connection(split[0], Integer.parseInt(split[1])));
				break;
			} catch (Exception e) {
				System.out.println("Invalid address.");
				continue;
			}
		}
		System.out.println();

		while (true) {
			System.out.print("Delimination>");
			String raw = Console.input.nextLine().trim();
			if (raw.isEmpty())
				continue;

			try {
				commands.executeCommand(lastCommand = CommandInterface.parseCommand(raw));
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