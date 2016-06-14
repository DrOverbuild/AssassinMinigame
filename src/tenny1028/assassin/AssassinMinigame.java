/*
 * Copyright (c) 2015. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.Main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import tenny1028.assassin.config.MainConfig;
import tenny1028.assassin.config.MapsConfig;
import tenny1028.assassin.config.MessagesConfig;
import tenny1028.assassin.events.PlayerEvents;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by jasper on 9/15/15.
 */
public class AssassinMinigame extends JavaPlugin{
	Player currentCoordinator = null;
//	Team team;
	AssassinCommand cmdExec;
	PlayerEvents pEvents;
	GameControl gc;
	MainConfig mainConfig;
	MapsConfig mapsConfig;
	MessagesConfig messagesConfig;

	@Override
	public void onEnable() {
		setupAssassin();
	}

	@Override
	public void onDisable() {
		for(OfflinePlayer p:getTeam().getPlayers()){
			if(p.isOnline()) {
				removePlayerFromGame(p.getPlayer());
			}
		}
	}

	public void setCurrentCoordinator(Player currentCoordinator) {

		if(this.currentCoordinator!=null){
			this.currentCoordinator.sendMessage(formatMessage("coordinator.no-longer"));
		}

		this.currentCoordinator = currentCoordinator;

		if(this.currentCoordinator != null){
			getLogger().info("Setting " + this.currentCoordinator.getName() + " as game coordinator.");
			currentCoordinator.sendMessage(formatMessage("coordinator.now-coordinator"));
		}else {
			getLogger().info("Setting null as game coordinator.");
		}
	}

	public GameControl getGameControl() {
		return gc;
	}

	public Team getTeam() {
		return getServer().getScoreboardManager().getMainScoreboard().getTeam("Assassin");
	}

	public Objective getAssassinScore(){
		return getServer().getScoreboardManager().getMainScoreboard().getObjective("assassinScore");
	}

	public void addToAssassinScore(Player p, int amount){
		Score s = getAssassinScore().getScore(p);
		s.setScore(s.getScore() + amount);
	}

	public void takeFromAssassinScore(Player p, int amount){
		Score s = getAssassinScore().getScore(p);
		s.setScore(s.getScore() - amount);
	}

	public void setupAssassin(){
		saveConfig();
		mainConfig = new MainConfig(this);
		messagesConfig = new MessagesConfig(this);
		mapsConfig = new MapsConfig(this);

		if(getServer().getScoreboardManager().getMainScoreboard().getTeam("Assassin") == null) {
			getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("Assassin");
		}

		getTeam().setPrefix(ChatColor.AQUA + "");
		getTeam().setSuffix(" " + formatMessage("game.suffix"));

		if(getServer().getScoreboardManager().getMainScoreboard().getObjective("assassinScore")==null) {
			getServer().getScoreboardManager().getMainScoreboard().registerNewObjective("assassinScore","");
		}

		OfflinePlayer[] players = getTeam().getPlayers().toArray(new OfflinePlayer[]{});
		for(OfflinePlayer p:players){
			getTeam().removePlayer(p);
		}

		cmdExec = new AssassinCommand(this);
		pEvents = new PlayerEvents(this);

		getCommand("assassin").setExecutor(cmdExec);
		getCommand("assassin").setTabCompleter(cmdExec);
		getServer().getPluginManager().registerEvents(pEvents, this);

		gc = new GameControl(this);

	}

	public boolean playerIsPlayingAssassin(Player p){
		return getTeam().hasPlayer(p);
	}

	/**
	 * Adds the specified player to the game.
	 * @param p
	 * @return true if player is already in game.
	 */
	public boolean addPlayerToGame(Player p){
		if(playerIsPlayingAssassin(p)){
			return true;
		}

		if(!Util.inventoryIsEmpty(p.getInventory())){
			p.sendMessage(formatMessage("commands.full-inventory"));
			return false;
		}

		getTeam().addPlayer(p);

		Location lobbySpawn = getMainConfig().getLobbySpawn();

		if(lobbySpawn != null) {
			p.teleport(lobbySpawn);
		}

		if(getGameControl().isCurrentlyInProgress()){
			p.setGameMode(GameMode.SPECTATOR);
		}else{
			p.setGameMode(GameMode.ADVENTURE);
		}
		p.setFoodLevel(20);
		for(Player p2:getServer().getOnlinePlayers()){
				p2.sendMessage(formatMessage("game.join","%player",p.getName(),"%cp",getNumberOfPlayersPlayingAssassin() + "","%mp",
						getMainConfig().getMinimumPlayers() + ""));
		}

		if(getNumberOfPlayersPlayingAssassin() == 1){
			setCurrentCoordinator(p);
		}
		return false;
	}

	public boolean removePlayerFromGame(Player p){
		return removePlayerFromGame(p,true);
	}

	public boolean removePlayerFromGame(Player p, boolean fullyRemovePlayer){
		if(!playerIsPlayingAssassin(p)){ // If player is not playing Assassin
			return true;                 // return true
		}

		boolean setCoordinator = false;

		if(currentCoordinator != null) {
			if (currentCoordinator.equals(p)) {
				setCoordinator = true;
			}
		}

		if(getNumberOfPlayersPlayingAssassin() == 1){
			setCurrentCoordinator(null);
			setCoordinator = false;
		}

		if(getGameControl().isCurrentlyInProgress()){
			if(getGameControl().getAssassin().equals(p)){
				getGameControl().endGame(0);
			}else if(getGameControl().alivePlayers().size()==2){
				getGameControl().endGame(1);
			}
		}

		clearMinigameRelatedItems(p);
		p.setGameMode(GameMode.SURVIVAL);
		for(Player p2:getServer().getOnlinePlayers()){
			p2.sendMessage(formatMessage("game.leave","%player",p.getName(),"%cp",(getNumberOfPlayersPlayingAssassin() - 1) + "","%mp",
					getMainConfig().getMinimumPlayers() + ""));
		}

		if(setCoordinator){

			for(OfflinePlayer offlinePlayer : getTeam().getPlayers()){
				if(offlinePlayer.isOnline() && p.getName() != offlinePlayer.getName()){
					setCurrentCoordinator(offlinePlayer.getPlayer());
					break;
				}
			}
		}

		if(fullyRemovePlayer){
			fullyRemovePlayerFromGame(p);
		}

		return false;
	}

	public void fullyRemovePlayerFromGame(Player p){
		getTeam().removePlayer(p);
		if(p.getBedSpawnLocation() != null){
			p.teleport(p.getBedSpawnLocation());
		}else{
			p.teleport(p.getWorld().getSpawnLocation());
		}
	}

	public int getNumberOfPlayersPlayingAssassin(){
		int teamSize = 0;
		for(OfflinePlayer player:getTeam().getPlayers()){
			if(player.isOnline()){
				teamSize++;
			}
		}
		return teamSize;
	}

	public void broadcastToAllPlayersPlayingAssassin(String message){
		for(OfflinePlayer p : getTeam().getPlayers()){
			if(p.isOnline()) {
				p.getPlayer().sendMessage(message);
			}
		}
	}

	public Set<Player> alivePlayers(){
		Set<Player> alivePlayers = new HashSet<>();

		for(OfflinePlayer p : getTeam().getPlayers()){
			if(p.isOnline()) {
				if (p.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
					alivePlayers.add(p.getPlayer());
				}
			}
		}

		return alivePlayers;
	}

	public void clearMinigameRelatedItems(Player p){
		ItemStack[] items = p.getInventory().getContents();
		for(ItemStack i:items){
			if(itemIsMinigameRelated(i)){
				p.getInventory().remove(i);
			}
		}
	}

	public boolean itemIsMinigameRelated(ItemStack i){
		if(i != null) {
			if (i.getItemMeta().hasLore()) {
				if (i.getItemMeta().getLore().size() > 0) {
					if (i.getItemMeta().getLore().get(0).equals(GameControl.LORE_MESSAGE)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void reloadConfigs(){
		reloadConfig();
		GameControl.ITEM_SPAWN_FREQUENCY = getConfig().getInt("scraps.seconds-per-spawn",15);
		GameControl.NUMBER_OF_ITEMS_TO_SPAWN = getConfig().getInt("scraps.items-per-spawn",4);
		GameControl.ITEM_SPAWN_DISTANCE = getConfig().getInt("scraps.spawn-radius",20);
		GameControl.CHANCE_BOW = getConfig().getDouble("scraps.chance-bow",0.4d);
		messagesConfig = new MessagesConfig(this);
	}

	public MainConfig getMainConfig(){
		return mainConfig;
	}

	public MapsConfig getMapsConfig() {
		return mapsConfig;
	}

	public MessagesConfig getMessagesConfig() {
		return messagesConfig;
	}

	public String formatMessage(String message, Map<String, String> variables){
		return this.getMessagesConfig().formatMessage(message,variables);
	}

	public String formatMessage(String message){
		return this.getMessagesConfig().formatMessage(message);
	}

	public String formatMessage(String message, String... variables){
		return this.getMessagesConfig().formatMessage(message,MessagesConfig.toMap(variables));
	}
}
