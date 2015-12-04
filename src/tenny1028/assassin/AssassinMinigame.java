/*
 * Copyright (c) 2015. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
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
	Team team;
	AssassinCommand cmdExec;
	PlayerEvents pEvents;
	GameControl gc;

	@Override
	public void onEnable() {
		setupAssassin();
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
		return team;
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

		team = getServer().getScoreboardManager().getMainScoreboard().getTeam("Assassin");
		team.setPrefix(ChatColor.AQUA + "");
		team.setSuffix(" (In Minigame)");

		for(OfflinePlayer p:team.getPlayers()){
			team.removePlayer(p);
		}

		if(getServer().getScoreboardManager().getMainScoreboard().getObjective("assassinScore")==null) {
			getServer().getScoreboardManager().getMainScoreboard().registerNewObjective("assassinScore","");
		}

		cmdExec = new AssassinCommand(this);
		pEvents = new PlayerEvents(this);

		if(cmdExec == null){
			getLogger().warning("cmdExec is null!");
		}

		if(getCommand("assassin") == null){
			getLogger().warning("Unable to find command assassin!");
		}
		getCommand("assassin").setExecutor(cmdExec);
		getServer().getPluginManager().registerEvents(pEvents, this);
		gc = new GameControl(this);
	}

	public boolean playerIsPlayingAssassin(Player p){
		return team.hasPlayer(p);
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

		team.addPlayer(p);
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

		if(team.getSize() == 1){
			setCurrentCoordinator(p);
		}
		return false;
	}

	public boolean removePlayerFromGame(Player p){
		if(!playerIsPlayingAssassin(p)){ // If player is not playing Assassin
			return true;                 // return true
		}

		boolean setCoordinator = false;

		if(currentCoordinator != null) {
			if (currentCoordinator.equals(p)) {
				setCoordinator = true;
			}
		}

		if(team.getSize() == 1){
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

		team.removePlayer(p);
		clearMinigameRelatedItems(p);
		p.setGameMode(GameMode.SURVIVAL);
		p.sendMessage(ChatColor.AQUA + "You are no longer playing Assassin.");
		for(Player p2:getServer().getOnlinePlayers()){
			if(!p2.equals(p)) {
				p2.sendMessage(ChatColor.AQUA + p.getName() + " is no longer playing Assassin.");
			}
		}

		if(setCoordinator){
			setCurrentCoordinator(((OfflinePlayer)team.getPlayers().toArray()[0]).getPlayer());
		}
		return false;
	}

	public void broadcastToAllPlayersPlayingAssassin(String message){
		for(OfflinePlayer p : team.getPlayers()){
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

}
