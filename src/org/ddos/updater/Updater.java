package org.ddos.updater;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public class Updater {
	public static final String NAME = "DeliminationCoreUpdater";

	public static void main(String[] args) {
		File target = new File(System.getProperty("user.dir") + "\\DeliminationCore" + args[0] + ".jar");

		try {
			System.out.println("Updating...");

			Thread.sleep(1000);

			if (target.exists())
				target.delete();

			target.createNewFile();

			FileUtils.copyURLToFile(
					new URL("https://raw.githubusercontent.com/Delmox/Delimination-Core/master/jars/DeliminationCore"
							+ args[0] + ".jar"),
					target);

			System.out.println("Updated!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void downloadIfNonexistent() throws IOException {
		if (!existsOnThisMachine()) {
			FileUtils.copyURLToFile(
					new URL("https://raw.githubusercontent.com/Delmox/Delimination-Core/master/jars/" + NAME + ".jar"),
					new File(System.getProperty("user.dir") + File.separator + NAME + ".jar"));
		}
	}

	public static boolean existsOnThisMachine() {
		return new File(System.getProperty("user.dir") + File.separator + NAME + ".jar").exists();
	}
}