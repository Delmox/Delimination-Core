package org.ddos.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Status implements Serializable {
	private static final long serialVersionUID = 3872435527895353572L;
	private ArrayList<ActiveAttack> attacks = new ArrayList<>();

	public ActiveAttack[] getAttacks() {
		return attacks.toArray(new ActiveAttack[attacks.size()]);
	}

	public void setAttacks(ActiveAttack... attacks) {
		this.attacks = new ArrayList<>(Arrays.asList(attacks));
	}

	public void addAttack(ActiveAttack attack) {
		attacks.add(attack);
	}

	public void removeAttack(ActiveAttack attack) {
		attacks.remove(attack);
	}

	public ActiveAttack getAttackByAddress(String ip) {
		for (ActiveAttack attack : attacks) {
			if (attack.getAttackingAddress().equals(ip))
				return attack;
		}
		return null;
	}
}