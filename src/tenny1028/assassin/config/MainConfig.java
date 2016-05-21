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

import java.util.Set;

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

	public Set<String> getMaps(){
		return getConfig().getConfigurationSection("maps").getKeys(false);
	}

	public Location getLobbySpawn(){
		Object spawn = getConfig().get("lobby-spawn");
		if(spawn != null && spawn instanceof Location){
			return (Location)spawn;
		}
		return null;
	}

	public Location getMapSpawn(String mapName){
		Object spawn = getConfig().get("maps." + mapName + ".spawn");
		if(spawn != null && spawn instanceof Location){
			return (Location)spawn;
		}
		return null;
	}

	public int getMinimumPlayers(){
		return getConfig().getInt("minimum-players",3);
	}

	public boolean hasMap(String mapName){
		return getConfig().contains("maps." + mapName);
	}

	public void removeMap(String mapName){
		getConfig().set("maps."+mapName,null);
		saveConfig();
	}

	public void saveConfig(){
		plugin.saveConfig();
	}

	public void setLobbySpawn(Location lobbySpawn){
		getConfig().set("lobby-spawn",lobbySpawn);
		saveConfig();
	}

	public void setMapSpawn(String mapName, Location spawn){
		getConfig().set("maps." + mapName + ".spawn",spawn);
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
