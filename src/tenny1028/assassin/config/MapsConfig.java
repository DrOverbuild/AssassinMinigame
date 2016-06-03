/*
 * Copyright (c) 2016. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin.config;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import tenny1028.assassin.AssassinMinigame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by jasper on 5/28/16.
 */
public class MapsConfig {
	AssassinMinigame plugin;

	public MapsConfig(AssassinMinigame plugin) {
		this.plugin = plugin;
	}

	public FileConfiguration getConfig(){
		return plugin.getConfig();
	}

	public Set<String> getMaps(){
		if(getConfig().getConfigurationSection("maps") == null){
			return Collections.emptySet();
		}
		return getConfig().getConfigurationSection("maps").getKeys(false);
	}

	public List<MapProtection> getMapsProtections(){
		List<MapProtection> mapProtections = new ArrayList<>();

		for(String map:getMaps()){
			if(mapIsProtected(map)) {
				mapProtections.add(new MapProtection(this, map));
			}
		}

		return mapProtections;
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

	public boolean mapHasDeadlyLiquid(String mapName){
		if(!getConfig().contains("maps."+mapName+".deadly-liquid")){
			getConfig().set("maps."+mapName+".deadly-liquid",false);
			saveConfig();
		}
		return getConfig().getBoolean("maps."+mapName+".deadly-liquid",false);
	}

	public boolean mapIsProtected(String mapName){
		return getConfig().contains("maps."+mapName+".protection.first") && getConfig().contains("maps."+mapName+".protection.second");
	}

	public void removeMap(String mapName){
		getConfig().set("maps."+mapName,null);
		saveConfig();
	}

	public void saveConfig(){
		plugin.saveConfig();
	}

	public void setMapDefaults(String mapName){
		if(!getConfig().contains("maps."+mapName+".deadly-liquid")){
			getConfig().set("maps."+mapName+".deadly-liquid",false);
			saveConfig();
		}
	}

	public void setMapSpawn(String mapName, Location spawn){
		getConfig().set("maps." + mapName + ".spawn",spawn);
		setMapDefaults(mapName);
		saveConfig();
	}
}
