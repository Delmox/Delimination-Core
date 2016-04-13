package org.ddos.command;

import org.ddos.util.Strings;
import org.jcom.CommandData;
import org.jcom.CommandInterface;
import org.jcom.FlagData;
import org.jcom.UnknownCommandException;

public class Commands {
	public static String getHelpForAction(String command, CommandInterface com) throws UnknownCommandException {
		int maxLengthUsage = 0;
		for (CommandData data : com.getCommandData(command)) {
			if (data.getUsage().length() > maxLengthUsage) {
				maxLengthUsage = data.getUsage().length();
			}
		}

		String help = "Command: " + command + "\nUsage(s):\n";

		for (CommandData data : com.getCommandData(command)) {
			help += "\t" + data.getUsage() + Strings.getString(maxLengthUsage - data.getUsage().length(), ' ') + "\t"
					+ data.getDescription() + "\n";

			help += "\tValid Flag(s):\n";
			for (FlagData flagData : data.getValidFlags()) {
				help += "\t\t" + flagData.getFlagName() + "\t" + flagData.getDescription() + "\n";
			}
			if (data.getValidFlags().length == 0) {
				help += "\t\tNone\n";
			}
		}

		return help;
	}
}