/*
 * Copyright (c) 2016. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tenny1028.assassin.AssassinMinigame;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jasper on 5/22/16.
 */
public class MessagesConfig {
	private AssassinMinigame plugin;
	private File file;
	private FileConfiguration config;

	public MessagesConfig(AssassinMinigame plugin) {
		this.plugin = plugin;

		file = new File(plugin.getDataFolder(),"messages.yml");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		config.addDefault("death.assassin-killed-civilian","&bThe Assassin has killed %p!");
		config.addDefault("death.civilian-killed-assassin","&c%assassin&b was slain by &a%civilian&b.");
		config.addDefault("death.civilian-killed-civilian","&b%killed was shot by %killer.");
		config.addDefault("death.civilian-died","&b%p died.");
		config.addDefault("commands.cannot-use","&cYou cannot use that command while playing Assassin!");

		config.options().copyDefaults(true);
		saveConfig();

	}

	public void saveConfig(){
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String formatMessage(String message,Map<String,String> variables){
		message = ChatColor.translateAlternateColorCodes('&',config.getString(message));

		for(String variable:variables.keySet()){
			message = message.replace(variable,variables.get(variable));
		}

		return message;
	}

	public String formatMessage(String message){
		return ChatColor.translateAlternateColorCodes('&',config.getString(message));
	}

	public static Map<String, String> toMap(String... variables){
		Map<String, String> map = new HashMap<>();
		for(int i = 0; i<variables.length; i++){
			if(i + 1 < variables.length){
				map.put(variables[i],variables[i+1]);
				i++;
			}
		}
		return map;
	}
}
