package org.ddos.util;

import java.io.Serializable;
import java.net.SocketAddress;

public class Computer implements Serializable {
	private static final long serialVersionUID = 1853047640997832841L;
	private SocketAddress address;
	private boolean isZombie;

	public Computer(SocketAddress addr, boolean zombie) {
		this.address = addr;
		this.isZombie = zombie;
	}

	public boolean isZombie() {
		return isZombie;
	}

	public void setIsZombie(boolean isZombie) {
		this.isZombie = isZombie;
	}

	public SocketAddress getAddress() {
		return address;
	}

	public void setAddress(SocketAddress address) {
		this.address = address;
	}
}