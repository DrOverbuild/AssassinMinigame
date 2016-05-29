/*
 * Copyright (c) 2016. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin.config;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import tenny1028.assassin.AssassinMinigame;

import java.util.Set;

/**
 * Created by jasper on 5/28/16.
 */
public class MapsConfig {
	AssassinMinigame plugin;

	public MapsConfig(AssassinMinigame plugin) {
		this.plugin = plugin;
	}

	private FileConfiguration getConfig(){
		return plugin.getConfig();
	}

	public Set<String> getMaps(){
		return getConfig().getConfigurationSection("maps").getKeys(false);
	}

	public Location getMapSpawn(String mapName){
		Object spawn = getConfig().get("maps." + mapName + ".spawn");
		if(spawn != null && spawn instanceof Location){
			return (Location)spawn;
		}
		return null;
	}

	public boolean hasMap(String mapName){
		return getConfig().contains("maps." + mapName);
	}

	public void removeMap(String mapName){
		getConfig().set("maps."+mapName,null);
		saveConfig();
	}

	private void saveConfig(){
		plugin.saveConfig();
	}

	public void setMapSpawn(String mapName, Location spawn){
		getConfig().set("maps." + mapName + ".spawn",spawn);
		saveConfig();
	}
}
