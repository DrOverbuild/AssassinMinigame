/*
 * Copyright (c) 2015. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import tenny1028.assassin.events.PlayerEvents;

import java.util.HashSet;
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
	Location spawn;

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
			this.currentCoordinator.sendMessage(ChatColor.AQUA + "You are no longer the game coordinator.");
		}

		this.currentCoordinator = currentCoordinator;

		if(this.currentCoordinator != null){
			getLogger().info("Setting " + this.currentCoordinator.getName() + " as game coordinator.");
			currentCoordinator.sendMessage(ChatColor.AQUA + "You are now the game coordinator.");
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
		if(getServer().getScoreboardManager().getMainScoreboard().getTeam("Assassin") == null) {
			getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("Assassin");
		}

		getTeam().setPrefix(ChatColor.AQUA + "");
		getTeam().setSuffix(" (Assassin)");

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
		getServer().getPluginManager().registerEvents(pEvents, this);
		gc = new GameControl(this);

		saveConfig();
		spawn = getLocationFromConfig(getConfig());
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
			p.sendMessage(ChatColor.RED + "You cannot join if you have items in your inventory.");
			return false;
		}

		getTeam().addPlayer(p);

		if(getSpawn() != null) {
			p.teleport(getSpawn());
		}

		if(getGameControl().isCurrentlyInProgress()){
			p.setGameMode(GameMode.SPECTATOR);
		}else{
			p.setGameMode(GameMode.ADVENTURE);
		}
		p.setFoodLevel(20);
		p.sendMessage(ChatColor.AQUA + "You are now playing Assassin.");
		for(Player p2:getServer().getOnlinePlayers()){
			if(!p2.equals(p)) {
				p2.sendMessage(ChatColor.AQUA + p.getName() + " is now playing Assassin.");
			}
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
		p.sendMessage(ChatColor.AQUA + "You are no longer playing Assassin.");
		for(Player p2:getServer().getOnlinePlayers()){
			if(!p2.equals(p)) {
				p2.sendMessage(ChatColor.AQUA + p.getName() + " is no longer playing Assassin.");
			}
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
			if(p.getPlayer().getGameMode().equals(GameMode.ADVENTURE)){
				alivePlayers.add(p.getPlayer());
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

	public Location getSpawn() {
		return spawn;
	}

	public void setSpawn(Location spawn) {
		this.spawn = spawn;
		saveLocationToConfig(getConfig(),spawn);
		saveConfig();
	}

	public static Location getLocationFromConfig(FileConfiguration config){
		if(!config.contains("spawn")){
			return null;
		}

		String worldName = config.getString("spawn.world","world");
		World world = Bukkit.getWorld(worldName);

		if(world == null){
			return null;
		}

		double x = config.getDouble("spawn.x",0);
		double y = config.getDouble("spawn.y",0);
		double z = config.getDouble("spawn.z",0);
		float yaw = (float)config.getInt("spawn.yaw",0);
		float pitch = (float)config.getInt("spawn.pitch");

		return new Location(world,x,y,z,yaw,pitch);
	}

	public static void saveLocationToConfig(FileConfiguration config, Location loc){
		if(loc == null){
			config.set("spawn",null);
		}

		config.set("spawn.world",loc.getWorld().getName());
		config.set("spawn.x",loc.getX());
		config.set("spawn.y",loc.getY());
		config.set("spawn.z",loc.getZ());
		config.set("spawn.yaw",loc.getYaw());
		config.set("spawn.pitch",loc.getPitch());
	}

}
