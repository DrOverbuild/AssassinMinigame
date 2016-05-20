/*
 * Copyright (c) 2016. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import tenny1028.assassin.AssassinMinigame;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages main config.
 */
public class MainConfig {
	private AssassinMinigame plugin;

	public MainConfig(AssassinMinigame plugin) {
		this.plugin = plugin;

		updateConfig();
	}

	private FileConfiguration getConfig(){
		return plugin.getConfig();
	}

	public List<Location> getMaps(){
		List<Location> maps = new ArrayList<>();
		for(String s:getConfig().getStringList("maps")){
			maps.add((Location)getConfig().get("maps." + s + ".spawn"));
		}
		return maps;
	}

	public Location getLobbySpawn(){
		if(getConfig().get("lobby-spawn") == null){
			return null;
		}
		return (Location)getConfig().get("lobby-spawn");
	}

	public int getMinimumPlayers(){
		return getConfig().getInt("minimum-players",3);
	}

	public void saveConfig(){
		plugin.saveConfig();
	}

	public void setLobbySpawn(Location lobbySpawn){
		getConfig().set("lobby-spawn",lobbySpawn);
		saveConfig();
	}

	/**
	 * Update to latest version of config if using old version.
	 */
	public void updateConfig(){
		if (getConfig().contains("spawn")){
			String worldName = getConfig().getString("spawn.world","world");
			World world = Bukkit.getWorld(worldName);

			if(world != null){
				double x = getConfig().getDouble("spawn.x",0);
				double y = getConfig().getDouble("spawn.y",0);
				double z = getConfig().getDouble("spawn.z",0);
				float yaw = (float)getConfig().getDouble("spawn.yaw",0);
				float pitch = (float)getConfig().getDouble("spawn.pitch");
				setLobbySpawn(new Location(world,x,y,z,yaw,pitch));
			}

			getConfig().set("spawn", null);
			saveConfig();
		}
	}
}
