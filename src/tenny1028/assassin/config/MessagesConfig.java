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

		config.addDefault("game.already-playing","&cYou are already playing Assassin.");
		config.addDefault("game.not-playing","&cYou are not playing Assassin");
		config.addDefault("game.must-be-playing","&cYou must be playing Assassin.");
		config.addDefault("game.not-enough-players","&cYou do not have enough players to start (%cp/%mp).");
		config.addDefault("game.in-progress","&cA game is already in progress!");
		config.addDefault("game.suffix","(Assassin)");
		config.addDefault("game.join","&b%player is now playing Assassin (%cp/%mp).");
		config.addDefault("game.leave","&b%player is no longer playing Assassin (%cp/%mp).");
		config.addDefault("death.assassin-killed-civilian","&bThe Assassin has killed %p!");
		config.addDefault("death.civilian-killed-assassin","&c%assassin&b was slain by &a%civilian&b.");
		config.addDefault("death.civilian-killed-civilian","&b%killed was shot by %killer.");
		config.addDefault("death.civilian-died","&b%p died.");
		config.addDefault("death.assassin-died","&bThe Assassin, &c%p&b, died.");
		config.addDefault("winner.civilians","&bThe civilians win!");
		config.addDefault("winner.assassin","&bThe assassin, &c%assassin&b, wins!");
		config.addDefault("winner.none","&bNo one wins.");
		config.addDefault("countdown.pregame","&aThe assassin will be chosen in &e%secondss");
		config.addDefault("commands.cannot-use","&cYou cannot use that command while playing Assassin!");
		config.addDefault("commands.no-permission","&cYou do not have permission.");
		config.addDefault("commands.invalid-syntax","&cInvalid Syntax. Type \"assassin help\" for help.");
		config.addDefault("commands.must-be-player","&cYou must be a player.");
		config.addDefault("commands.not-coordinator","&cYou must be the game coordinator to choose the map.");
		config.addDefault("commands.full-inventory","&cYou cannot join if you have items in your inventory.");
		config.addDefault("commands.help.header","------------ &bAssassin Minigame Help&r ------------");
		config.addDefault("commands.help.config","&b/%cmd&r: Configure AssassinMinigame");
		config.addDefault("commands.help.join","&b/%cmd&r: Join Assassin");
		config.addDefault("commands.help.leave","&b/%cmd&r: Leave Assassin");
		config.addDefault("commands.help.leaderboards","&b/%cmd&r: Show the top 5 players in Assassin");
		config.addDefault("commands.help.map","&b/%cmd <map>&r: Set the map for the next game");
		config.addDefault("commands.help.maps","&b/%cmd&r: Show all available maps");
		config.addDefault("commands.help.start","&b/%cmd&r: Start the game");
		config.addDefault("commands.help.tp","&b/%cmd <map>&r: Teleport to the map spawn");
		config.addDefault("map.does-not-exist","&cMap %map does not exist.");
		config.addDefault("map.no-current-map","&bThere is no current map.");
		config.addDefault("map.current-map","&bThe current map is &l%map&b.");
		config.addDefault("map.no-maps","&bThere are no configured maps.");
		config.addDefault("map.chosen","&bMap &l%map&b has been chosen.");
		config.addDefault("map.during-game","&cYou cannot change the map during the game.");
		config.addDefault("protection.protected","&cThis area is protected.");
		config.addDefault("protection.cannot-leave","&cYou cannot leave this area while playing Assassin");
		config.addDefault("leaderboard.header","&b------------ &lTop 5 Scores For Assassin&r&b ------------");
		config.addDefault("leaderboard.element","&b&l%player&r: %points points");
		config.addDefault("leaderboard.divider","&b ................");
		config.addDefault("leaderboard.footer","&b----------------------------------------------------");
		config.addDefault("title.civilian","&bYou're a civilian");
		config.addDefault("title.archer","&aYou're the archer");
		config.addDefault("title.assassin","&cYou're the assassin");
		config.addDefault("subtitle.civilian","Stay alive!");
		config.addDefault("subtitle.archer","Find the assassin and protect the civilians!");
		config.addDefault("subtitle.assassin","Kill everyone!");
		config.addDefault("coordinator.no-longer","&bYou are no longer the game coordinator.");
		config.addDefault("coordinator.now-coordinator","&bYou are now the game coordinator.");
		config.addDefault("items.archers-bow","&bArcher's Bow");
		config.addDefault("items.lore-message","&bFor use with the Assassin minigame.");

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
