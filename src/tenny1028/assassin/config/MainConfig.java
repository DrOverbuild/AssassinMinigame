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

		getConfig().addDefault("countdown.game",600);
		getConfig().addDefault("countdown.pregame",10);
		getConfig().addDefault("cooldown.sword",2);
		getConfig().addDefault("cooldown.bow",1);
		getConfig().addDefault("cooldown.arrow-refresh",1);
		getConfig().addDefault("cooldown.max-arrows",3);
		getConfig().addDefault("scraps.seconds-per-spawn",15);
		getConfig().addDefault("scraps.items-per-spawn",4);
		getConfig().addDefault("scraps.spawn-radius",20);
		getConfig().addDefault("scraps.chance-bow",0.4d);
		getConfig().addDefault("events.civilian-shoot-civilian.kill-damager",true);
		getConfig().addDefault("events.civilian-shoot-civilian.kill-damaged",true);
		getConfig().addDefault("announce-death-messages",false);
		getConfig().options().copyDefaults(true);

		updateConfig();

		saveConfig();
	}

	public boolean getAnnounceDeathMessages() {
		return getConfig().getBoolean("announce-death-messages",false);
	}

	public int getArrowRefresh(){
		return getConfig().getInt("cooldown.arrow-refresh",1);
	}

	public int getBowCooldown(){
		return getConfig().getInt("cooldown.bow",1);
	}

	private FileConfiguration getConfig(){
		return plugin.getConfig();
	}

	public int getGameCountdown(){
		return getConfig().getInt("countdown.game",600);
	}

	public Location getLobbySpawn(){
		Object spawn = getConfig().get("lobby-spawn");
		if(spawn != null && spawn instanceof Location){
			return (Location)spawn;
		}
		return null;
	}

	public int getMaxArrows(){
		return getConfig().getInt("cooldown.max-arrows",3);
	}

	public int getMinimumPlayers(){
		int minimumPlayers = getConfig().getInt("minimum-players",3);
		return Math.max(3,minimumPlayers);
	}

	public int getPregameCountdown(){
		return getConfig().getInt("countdown.pregame",10);
	}

	public int getSwordCooldown(){
		return getConfig().getInt("cooldown.sword",3);
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
