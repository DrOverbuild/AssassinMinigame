/*
 * Copyright (c) 2016. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin.config;

import org.bukkit.Location;

/**
 * Created by jasper on 5/31/16.
 */
public class MapProtection {
	MapsConfig mapsConfig;
	String map;

	public MapProtection(MapsConfig mapsConfig, String map) {
		this.mapsConfig = mapsConfig;
		this.map = map;
	}

	public void addProtection(Location loc1, Location loc2){
		String path = "maps."+map+".protection";

		mapsConfig.getConfig().set(path + ".first.x",loc1.getBlockX());
		mapsConfig.getConfig().set(path + ".first.y",loc1.getBlockY());
		mapsConfig.getConfig().set(path + ".first.z",loc1.getBlockZ());

		mapsConfig.getConfig().set(path + ".second.x",loc2.getBlockX());
		mapsConfig.getConfig().set(path + ".second.y",loc2.getBlockY());
		mapsConfig.getConfig().set(path + ".second.z",loc2.getBlockZ());

		mapsConfig.getConfig().set(path + ".allow-interaction",true);
		mapsConfig.getConfig().set(path + ".allow-mob-spawn",false);
		mapsConfig.getConfig().set(path + ".allow-world-modification",false);
		mapsConfig.getConfig().set(path + ".restrict-players",false);
	}

	public boolean allowInteraction(){
		return mapsConfig.getConfig().getBoolean("maps." + map + ".protection.allow-interaction",true);
	}

	public boolean allowMobSpawn(){
		return mapsConfig.getConfig().getBoolean("maps." + map + ".protection.allow-mob-spawn",true);
	}

	public boolean allowWorldModification() {
		return mapsConfig.getConfig().getBoolean("maps." + map + ".protection.allow-world-modification", false);
	}

	public boolean locationIsInProtectedArea(Location loc){
		String path = "maps." + map + ".protection";

		if(mapsConfig.getConfig().contains(path)) {

			if(mapsConfig.getConfig().contains(path + "first")){
				return false;
			}

			if(mapsConfig.getConfig().contains(path + "second")){
				return false;
			}

			int x1 = mapsConfig.getConfig().getInt(path + ".first.x", 0);
			int y1 = mapsConfig.getConfig().getInt(path + ".first.y", 0);
			int z1 = mapsConfig.getConfig().getInt(path + ".first.z", 0);

			Location loc1 = new Location(loc.getWorld(), x1, y1, z1);

			int x2 = mapsConfig.getConfig().getInt(path + ".second.x", 0);
			int y2 = mapsConfig.getConfig().getInt(path + ".second.y", 0);
			int z2 = mapsConfig.getConfig().getInt(path + ".second.z", 0);

			Location loc2 = new Location(loc.getWorld(), x2, y2, z2);

			return loc.toVector().isInAABB(loc1.toVector(), loc2.toVector());
		}
		return false;
	}

	public void setLocation1(Location loc1){
		String path = "maps."+map+".protection";

		mapsConfig.getConfig().set(path + ".first.x",loc1.getBlockX());
		mapsConfig.getConfig().set(path + ".first.y",loc1.getBlockY());
		mapsConfig.getConfig().set(path + ".first.z",loc1.getBlockZ());

		mapsConfig.getConfig().addDefault(path + ".allow-interaction",true);
		mapsConfig.getConfig().addDefault(path + ".allow-mob-spawn",false);
		mapsConfig.getConfig().addDefault(path + ".allow-world-modification",false);
		mapsConfig.getConfig().addDefault(path + ".restrict-players",false);
		mapsConfig.getConfig().options().copyDefaults(true);
		mapsConfig.saveConfig();
	}

	public void setLocation2(Location loc2){
		String path = "maps."+map+".protection";

		mapsConfig.getConfig().set(path + ".second.x",loc2.getBlockX());
		mapsConfig.getConfig().set(path + ".second.y",loc2.getBlockY());
		mapsConfig.getConfig().set(path + ".second.z",loc2.getBlockZ());

		mapsConfig.getConfig().addDefault(path + ".allow-interaction",true);
		mapsConfig.getConfig().addDefault(path + ".allow-mob-spawn",false);
		mapsConfig.getConfig().addDefault(path + ".allow-world-modification",false);
		mapsConfig.getConfig().addDefault(path + ".restrict-players",false);
		mapsConfig.getConfig().options().copyDefaults(true);
		mapsConfig.saveConfig();
	}

	public boolean restrictPlayers(){
		return mapsConfig.getConfig().getBoolean("maps." + map + ".protection.restrict-players",true);
	}
}
