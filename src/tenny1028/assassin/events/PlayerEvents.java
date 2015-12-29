/*
 * Copyright (c) 2015. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin.events;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tenny1028.assassin.AssassinMinigame;
import tenny1028.assassin.GameControl;

import java.util.Collections;

/**
 * Created by jasper on 9/15/15.
 */
public class PlayerEvents implements Listener {
	AssassinMinigame controller;

	public PlayerEvents(AssassinMinigame controller) {
		this.controller = controller;
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e){
		controller.removePlayerFromGame(e.getPlayer());
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent e){

		Player damager = null;
		Player damaged = null;

		if(entityIsPlayer(e.getDamager())){
			damager = (Player)e.getDamager();

			if(controller.playerIsPlayingAssassin(damager)){
				e.setCancelled(true);
			}else{
				damager = null;
			}
		}

		if(entityIsPlayer(e.getEntity())){
			damaged = (Player)e.getEntity();

			if(controller.playerIsPlayingAssassin(damaged)){
				e.setCancelled(true);
			}else{
				damaged = null;
			}
		}

		if(damaged == null || damager == null) return;

		if(!controller.getGameControl().isCurrentlyInProgress()) return;

		if(!controller.getGameControl().getAssassin().equals(damager)) return;

		if(damager.getItemInHand() == null) return;

		if(!damager.getItemInHand().getType().equals(Material.IRON_SWORD)) return;

		killPlayer(damaged,damager);
	}

	@EventHandler
	public void onPlayerHitWithBow(EntityDamageByEntityEvent e){

		if(!e.getDamager().getType().equals(EntityType.ARROW)) return;

		Arrow arrow = (Arrow)e.getDamager();

		Player damaged = null;
		Player damager = null;

		if(entityIsPlayer(e.getEntity())){
			damaged = (Player)e.getEntity();

			if(controller.playerIsPlayingAssassin(damaged)){
				e.setCancelled(true);
			}else{
				damaged = null;
			}
		}

		if(arrow.getShooter() instanceof Player){
			damager = (Player)arrow.getShooter();

			if(controller.playerIsPlayingAssassin(damager)){
				e.setCancelled(true);
			}else{
				damager = null;
			}
		}

		if(damaged == null || damager == null) return;

		if(controller.getGameControl().isCurrentlyInProgress()) {
			controller.getLogger().info("killPlayer("+damaged.getName()+","+damager.getName()+");");
			killPlayer(damaged, damager);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDamage(EntityDamageEvent e){
		if(entityIsPlayer(e.getEntity())){
			if(controller.playerIsPlayingAssassin((Player)e.getEntity())){
				e.setCancelled(true);
			}
		}
	}

	public void killPlayer(Player damaged, Player damager) {
		damaged.setGameMode(GameMode.SPECTATOR);
		ItemStack[] items = damaged.getInventory().getContents();
		for (ItemStack i : items) {
			if (i != null) {
				damaged.getWorld().dropItemNaturally(damaged.getLocation(), i);
			}
		}
		damaged.getInventory().clear();
		if (controller.getGameControl().getAssassin().equals(damager)) {
			controller.broadcastToAllPlayersPlayingAssassin(ChatColor.AQUA + "The Assassin has killed " + damaged.getName() + "!");
			controller.addToAssassinScore(damager,2);
			if (controller.getGameControl().alivePlayers().size() == 1) {
				controller.getGameControl().endGame(1);
			}
		}else if(controller.getGameControl().getAssassin().equals(damaged)){
			controller.broadcastToAllPlayersPlayingAssassin(ChatColor.RED + damaged.getName() + ChatColor.AQUA
					                               + " was slain by " + ChatColor.GREEN + damager.getName() );
			controller.addToAssassinScore(damager,5);
			controller.getGameControl().endGame(0);
		}else{
			controller.broadcastToAllPlayersPlayingAssassin(ChatColor.AQUA + damaged.getName() + " has died!");
			controller.takeFromAssassinScore(damager,10);
			if(controller.alivePlayers().size() == 1){
				controller.getGameControl().endGame(1);
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e){
		if(controller.playerIsPlayingAssassin(e.getPlayer())){
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerPickUpItem(PlayerPickupItemEvent e){
		if(controller.playerIsPlayingAssassin(e.getPlayer())){
			e.setCancelled(true);
			if(controller.getGameControl().isCurrentlyInProgress()) {
				if(!controller.getGameControl().getAssassin().equals(e.getPlayer())) {
					if (controller.itemIsMinigameRelated(e.getItem().getItemStack())) {
						if((e.getItem().getItemStack().getType().equals(Material.BOW))){
							if(e.getItem().getItemStack().getEnchantments().containsKey(Enchantment.ARROW_INFINITE)&&e.getPlayer().getInventory().contains(Material.BOW)){
								e.getPlayer().getInventory().remove(Material.BOW);
								e.setCancelled(false);
							}else if(!e.getPlayer().getInventory().contains(Material.BOW)){
								e.setCancelled(false);
							}
						}else if(e.getItem().getItemStack().getType().equals(Material.ARROW)){
							ItemStack infinityBow = new ItemStack(Material.BOW);
							infinityBow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
							ItemMeta m = infinityBow.getItemMeta();
							m.setLore(Collections.singletonList(GameControl.LORE_MESSAGE));
							infinityBow.setItemMeta(m);
							if(!(e.getPlayer().getInventory().contains(infinityBow)&&e.getPlayer().getInventory().contains(Material.ARROW))){
								e.setCancelled(false);
							}
						}else{
							e.setCancelled(false);
						}
					}
				}
			}
		}else{
			if(controller.itemIsMinigameRelated(e.getItem().getItemStack())){
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerHungerLevelChange(FoodLevelChangeEvent e){
		if(!entityIsPlayer(e.getEntity())) return;
		Player p = (Player)e.getEntity();

		if(controller.playerIsPlayingAssassin(p)){
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onChestOpen(InventoryOpenEvent e){
		if (e.getInventory().getHolder() instanceof Chest || e.getInventory().getHolder() instanceof DoubleChest || e.getInventory().getHolder() instanceof Furnace){
			if(entityIsPlayer(e.getPlayer())){
				Player p = (Player)e.getPlayer();
				if(controller.playerIsPlayingAssassin(p)){
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e){
		if(!e.getPlayer().hasPermission("assassin.op")) {
			if(controller.playerIsPlayingAssassin(e.getPlayer())) {
				if (!e.getMessage().startsWith("/assassin")) {
					e.setCancelled(true);
					e.getPlayer().sendMessage(ChatColor.RED + "You cannot use that command while playing Assassin!");
				}
			}
		}
	}

	public boolean entityIsPlayer(Entity e){
		return e.getType().equals(EntityType.PLAYER);
	}
}
