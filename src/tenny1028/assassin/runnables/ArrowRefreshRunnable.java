/*
 * Copyright (c) 2016. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin.runnables;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import tenny1028.assassin.AssassinMinigame;
import tenny1028.assassin.GameControl;
import tenny1028.assassin.Util;

import java.util.Collections;

/**
 * Created by jasper on 6/2/16.
 */
public class ArrowRefreshRunnable extends BukkitRunnable{
	private AssassinMinigame plugin;
	final int maxArrows;

	public ArrowRefreshRunnable(int maxArrows, AssassinMinigame plugin) {
		this.maxArrows = maxArrows;
		this.plugin = plugin;
	}

	@Override
	public void run() {
		if(!plugin.getGameControl().isCurrentlyInProgress()){
			this.cancel();
			return;
		}

		for(Player p : plugin.alivePlayers()){
			if(p.getInventory().contains(Material.BOW)){
				ItemStack bow = p.getInventory().getItem(p.getInventory().first(Material.BOW));
				if(bow.hasItemMeta() && bow.getItemMeta().hasDisplayName() && bow.getItemMeta().getDisplayName().equals(plugin.formatMessage("items.archers-bow"))){
					if(Util.getAmountOfMaterial(p.getInventory(),Material.ARROW) < maxArrows){
						p.getInventory().addItem(GameControl.arrow());
					}
				}
			}
		}
	}
}
