/*
 * Copyright (c) 2016. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin.runnables;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tenny1028.assassin.AssassinMinigame;

/**
 * Created by jasper on 6/2/16.
 */
public class CooldownRunnable extends BukkitRunnable {
	private final int cooldownTime;
	private int cooldownTimer;
	private Player player;
	private AssassinMinigame plugin;

	public CooldownRunnable(int cooldownTime, Player player, AssassinMinigame plugin) {
		this.cooldownTime = cooldownTime;
		this.cooldownTimer = cooldownTime;
		this.player = player;
		this.plugin = plugin;


		plugin.getGameControl().getCooldownPlayers().add(player.getName());
		player.setExp(1.0f);
	}

	@Override
	public void run() {
		cooldownTimer--;
		player.setExp((float)cooldownTimer/(float)cooldownTime);
		if(cooldownTimer <= 0){
			plugin.getGameControl().getCooldownPlayers().remove(player.getName());
			this.cancel();
		}
	}
}
