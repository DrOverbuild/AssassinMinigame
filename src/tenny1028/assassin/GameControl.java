/*
 * Copyright (c) 2015. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

import java.util.*;


/**
 * Created by jasper on 9/15/15.
 */
public class GameControl {
	public static final String LORE_MESSAGE = "For use with the Assassin minigame.";
	public static final int ITEM_SPAWN_DISTANCE = 100;
	public static final int ITEM_SPAWN_FREQUENCY = 15;
	public static final int NUMBER_OF_ITEMS_TO_SPAWN = 4;

	AssassinMinigame controller;
	boolean currentlyInProgress;
	boolean preGameCountdownStarted = false;
	int secondsLeft = 0;
	BukkitRunnable gameTimer = null;

	Player assassin = null;

	public GameControl(AssassinMinigame controller) {
		this.controller = controller;
	}

	public boolean isCurrentlyInProgress() {
		return currentlyInProgress;
	}

	public Player getAssassin() {
		return assassin;
	}

	public void startCountdown(Player starter){
		if(controller.getTeam().getSize()<3){
			controller.currentCoordinator.sendMessage(ChatColor.RED + "You do not have enough players to start.");
			return;
		}

		if(isCurrentlyInProgress()||preGameCountdownStarted){
			controller.currentCoordinator.sendMessage(ChatColor.RED + "A game is already in progress!");
			return;
		}

		ArrayList<OfflinePlayer> players = new ArrayList<>(controller.getTeam().getPlayers());

		for(OfflinePlayer p:players){
			if(p.isOnline()) {
				if (controller.getSpawn() != null) {
					p.getPlayer().teleport(controller.getSpawn());
				} else {
					p.getPlayer().teleport(starter);
				}
			}
		}

		preGameCountdownStarted = true;

		new BukkitRunnable(){
			int secondsLeft = 30;
			@Override
			public void run() {
				if(secondsLeft%5 == 0 || secondsLeft<5){
					controller.broadcastToAllPlayersPlayingAssassin(ChatColor.GREEN + "The assassin will be chosen in " + ChatColor.YELLOW + secondsLeft + "s");
				}
				secondsLeft--;
				if(secondsLeft < 0){
					cancel();
					startGame();
				}
			}
		}.runTaskTimer(controller, 0L, 20L);
	}

	public void startGame(){
		preGameCountdownStarted = false;
		currentlyInProgress = true;
		Random r = new Random();

		ArrayList<OfflinePlayer> players = new ArrayList<>(controller.getTeam().getPlayers());

		for(OfflinePlayer p : controller.getTeam().getPlayers()){
			if(!p.isOnline()){
				players.remove(p);
			}
		}

		for(OfflinePlayer p:players){
			p.getPlayer().getInventory().clear();
			p.getPlayer().getInventory().setHeldItemSlot(0);
		}

		assassin = players.remove(r.nextInt(players.size())).getPlayer();

		Player archer = players.remove(r.nextInt(players.size())).getPlayer();

		for(OfflinePlayer p : players){
			controller.getServer().dispatchCommand(controller.getServer().getConsoleSender(),"title " + p.getName() + " subtitle \"Stay alive!\"");
			controller.getServer().dispatchCommand(controller.getServer().getConsoleSender(),"title " + p.getName() + " title \"You're a civilian\"");
		}

		controller.getServer().dispatchCommand(controller.getServer().getConsoleSender(),"title " + archer.getName() + " subtitle \"Find the assassin and protect the civilians!\"");
		controller.getServer().dispatchCommand(controller.getServer().getConsoleSender(), "title " + archer.getName() + " title \"You're the archer\"");

		controller.getServer().dispatchCommand(controller.getServer().getConsoleSender(), "title " + assassin.getName() + " subtitle \"Kill everyone!\"");
		controller.getServer().dispatchCommand(controller.getServer().getConsoleSender(), "title " + assassin.getName() + " title \"You're the assassin\"");

		ItemStack infinityBow = new ItemStack(Material.BOW);
		infinityBow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		ItemMeta infinityBowMeta = infinityBow.getItemMeta();
		infinityBowMeta.setLore(Collections.singletonList(LORE_MESSAGE));
		infinityBow.setItemMeta(infinityBowMeta);

		ItemStack arrow = new ItemStack(Material.ARROW);
		ItemMeta arrowMeta = arrow.getItemMeta();
		arrowMeta.setLore(Collections.singletonList(LORE_MESSAGE));
		arrow.setItemMeta(arrowMeta);

		ItemStack sword = new ItemStack(Material.IRON_SWORD);
		ItemMeta swordMeta = sword.getItemMeta();
		swordMeta.setLore(Collections.singletonList(LORE_MESSAGE));
		sword.setItemMeta(swordMeta);

		assassin.getInventory().setItem(1,sword);
		//assassin.getInventory().setItem(2,infinityBow);
		//assassin.getInventory().setItem(8,arrow);
		archer.getInventory().setItem(1, infinityBow);
		archer.getInventory().setItem(8,arrow);

		secondsLeft = 600;

		gameTimer = new BukkitRunnable(){
			@Override
			public void run() {
				if(secondsLeft == 600 || secondsLeft == 300 || secondsLeft == 180 || secondsLeft == 60
						|| secondsLeft == 30 || secondsLeft == 20 || secondsLeft<10){
					if(secondsLeft >= 60) {
						controller.broadcastToAllPlayersPlayingAssassin(ChatColor.GREEN + "The round will end in " + ChatColor.YELLOW + secondsLeft/60 + " minutes");
					}else{
						controller.broadcastToAllPlayersPlayingAssassin(ChatColor.GREEN + "The round will end in " + ChatColor.YELLOW + secondsLeft + " seconds");
					}
				}

				if(secondsLeft%ITEM_SPAWN_FREQUENCY==0){
					for(int i=0;i<NUMBER_OF_ITEMS_TO_SPAWN;i++) {
						spawnRandomItems();
					}
				}
				secondsLeft--;
				if(secondsLeft < 0){
					endGame(0);
				}
			}
		};
		gameTimer.runTaskTimer(controller,0L,20L);
	}

	/**
	 * Ends the game, stopping the timer, clearing players' inventories, and announcing the winner.
	 * @param winner If the winner is 0, then the winner is not the assassin. If the winner is 1, then the winner is the
	 *               assassin.
	 */
	public void endGame(int winner){
		gameTimer.cancel();
		currentlyInProgress = false;
		// Winner:  0 = civilians won
		// Winner:  1 = assassin won

		try {
			if (winner == 0) {
				controller.broadcastToAllPlayersPlayingAssassin(ChatColor.AQUA + "The civilians won!");
				for (OfflinePlayer p:controller.getTeam().getPlayers()){
					if(p.isOnline()) {
						if (assassin != p.getPlayer()) {
							controller.addToAssassinScore(p.getPlayer(), 5);
						}
					}
				}
			} else if (winner == 1) {
				controller.broadcastToAllPlayersPlayingAssassin(ChatColor.AQUA + "The assassin, " + ChatColor.RED + assassin.getName() + ChatColor.AQUA + ", has won!");
				controller.addToAssassinScore(assassin,5);
			}

			for (OfflinePlayer p : controller.getTeam().getPlayers()) {
				if(p.isOnline()) {
					p.getPlayer().teleport(assassin);
					p.getPlayer().setGameMode(GameMode.ADVENTURE);
					p.getPlayer().getInventory().clear();
				}
			}

			for(Item i : assassin.getWorld().getEntitiesByClass(Item.class)){
				if(controller.itemIsMinigameRelated(i.getItemStack())){
					i.remove();
				}
			}

			assassin = null;
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public Set<Player> alivePlayers(){
		Set<Player> alivePlayers = new HashSet<>();

		for(OfflinePlayer p:controller.getTeam().getPlayers()){
			if(p.isOnline()) {
				if (p.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
					alivePlayers.add(p.getPlayer());
				}
			}
		}
		return alivePlayers;
	}

	public void spawnRandomItems(){
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta bowMeta = bow.getItemMeta();
		bowMeta.setLore(Collections.singletonList(LORE_MESSAGE));
		bow.setItemMeta(bowMeta);
		ItemStack arrow = new ItemStack(Material.ARROW);
		ItemMeta arrowMeta = arrow.getItemMeta();
		arrowMeta.setLore(Collections.singletonList(LORE_MESSAGE));
		arrow.setItemMeta(arrowMeta);

		Random r = new Random();

		double[] centerPoints = {0d,0d,0d};

		World w = null;

		for(Player p:alivePlayers()){
			centerPoints[0] += p.getLocation().getX();
			centerPoints[1] += p.getLocation().getY();
			centerPoints[2] += p.getLocation().getZ();
			if(w==null){
				w = p.getWorld();
			}
		}

		double size = (double)alivePlayers().size();
		Location center = new Location(w,centerPoints[0]/size,centerPoints[1]/size,centerPoints[2]/size);

		org.bukkit.util.Vector spawnLocationFromPlayer = new Vector(r.nextInt(ITEM_SPAWN_DISTANCE)-ITEM_SPAWN_DISTANCE/2,
				r.nextInt(ITEM_SPAWN_DISTANCE/2),
				r.nextInt(ITEM_SPAWN_DISTANCE)-ITEM_SPAWN_DISTANCE/2);
		Location spawnLocation = center.add(spawnLocationFromPlayer);

		spawnLocation.getWorld().dropItemNaturally(spawnLocation, (r.nextInt(10)>6)?bow:arrow);

	}
}
