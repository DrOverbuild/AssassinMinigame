/*
 * Copyright (c) 2016. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin;

import com.connorlinfoot.titleapi.TitleAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by jasper on 5/12/16.
 */
public class TitleManager {

	public static void sendTitle(Player p, String title, String subtitle){
		if(Bukkit.getPluginManager().getPlugin("TitleAPI") != null){
			TitleAPI.sendTitle(p,10,60,10,title,subtitle);
		} else {
			p.sendMessage(title + " " + ChatColor.RESET + subtitle);
		}
	}

}
