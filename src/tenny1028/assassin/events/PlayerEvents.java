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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tenny1028.assassin.AssassinMinigame;
import tenny1028.assassin.GameControl;
import tenny1028.assassin.config.MapProtection;
import tenny1028.assassin.config.MessagesConfig;
import tenny1028.assassin.runnables.CooldownRunnable;

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
		controller.removePlayerFromGame(e.getPlayer(),false);
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent e){
		controller.removePlayerFromGame(e.getPlayer(),false);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		if(controller.playerIsPlayingAssassin(e.getPlayer())){
			controller.fullyRemovePlayerFromGame(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent e){

		Player damager = null;
		Player damaged = null;

		if(entityIsPlayer(e.getDamager())){
			damager = (Player)e.getDamager();

			if(controller.playerIsPlayingAssassin(damager) && !controller.getGameControl().getCooldownPlayers().contains(damager.getName())){
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

		final int cooldownTime = controller.getMainConfig().getSwordCooldown() * 20;
		if(cooldownTime > 0){
			new CooldownRunnable(cooldownTime,damager,controller).runTaskTimer(controller,1,1);
		}

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

		if(controller.getGameControl().isCurrentlyInProgress()&&!damaged.equals(damager)) {
			if (controller.getGameControl().getAssassin().getName().equals(damaged.getName()) ||
					controller.getConfig().getBoolean("events.civilian-shoot-civilian.kill-damaged",true)){
				killPlayer(damaged, damager);
			}else if(controller.getConfig().getBoolean("events.civilian-shoot-civilian-kill-damager",true)){
				killPlayer(damager, null);
			}
		}
	}

	@EventHandler
	public void onArrowShoot(ProjectileLaunchEvent e){
		if(e.getEntityType().equals(EntityType.ARROW)){
			if(e.getEntity().getShooter() instanceof Player){
				Player shooter = (Player)e.getEntity().getShooter();
				if(controller.playerIsPlayingAssassin(shooter)){
					if(controller.getGameControl().getCooldownPlayers().contains(shooter.getName())) {
						e.setCancelled(true);
					}else{
						final int cooldownTime = controller.getMainConfig().getBowCooldown() * 20;
						if (cooldownTime > 0) {
							new CooldownRunnable(cooldownTime, shooter, controller).runTaskTimer(controller, 1, 1);
						}
					}
				}
			}
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
		if(damager != null) {
			if (controller.getGameControl().getAssassin().equals(damager)) {
//				controller.broadcastToAllPlayersPlayingAssassin(ChatColor.AQUA + "The Assassin has killed " + damaged.getName() + "!");
				broadcastDeathMessage(controller.formatMessage("death.assassin-killed-civilian",
						MessagesConfig.toMap("%p",damaged.getName())));
				controller.addToAssassinScore(damager, 2);
				if (controller.getGameControl().alivePlayers().size() == 1) {
					controller.getGameControl().endGame(1);
				}
			} else if (controller.getGameControl().getAssassin().equals(damaged)) {
//				controller.broadcastToAllPlayersPlayingAssassin(ChatColor.RED + damaged.getName() + ChatColor.AQUA
//						+ " was slain by " + ChatColor.GREEN + damager.getName());
				broadcastDeathMessage(controller.formatMessage("death.civilian-killed-assassin",
						MessagesConfig.toMap("%civilian",damager.getName(),"%assassin",damaged.getName())));
				controller.addToAssassinScore(damager, 5);
				controller.getGameControl().endGame(0);
			} else {
//				controller.broadcastToAllPlayersPlayingAssassin(ChatColor.AQUA + damaged.getName() + " was shot by " + damager.getName() + "!");
				broadcastDeathMessage(controller.formatMessage("death.civilian-killed-civilian",
						MessagesConfig.toMap("%killed",damaged.getName(),"%killer",damager.getName())));
				controller.takeFromAssassinScore(damager, 10);

				if(controller.getConfig().getBoolean("events.civilian-shoot-civilian-kill-damager",true)) {
					if (controller.alivePlayers().size() == 1) {
						controller.getGameControl().endGame(1);
						return;
					}
					killPlayer(damager, null);
				}
			}
		}else if(controller.getGameControl().getAssassin().getName().equals(damaged.getName())){
			broadcastDeathMessage(controller.formatMessage("death.assassin-died","%p",damaged.getName()));
			controller.getGameControl().endGame(0);
		}
		else{
//			controller.broadcastToAllPlayersPlayingAssassin(ChatColor.AQUA + damaged.getName() + " died!");
			broadcastDeathMessage(controller.formatMessage("death.civilian-died",
					MessagesConfig.toMap("%p",damaged.getName())));
			if(controller.getGameControl().alivePlayers().size() == 1){
				controller.getGameControl().endGame(1);
			}
		}
	}

	public void broadcastDeathMessage(String message){
		if(controller.getMainConfig().getAnnounceDeathMessages()){
			controller.broadcastToAllPlayersPlayingAssassin(message);
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
//					e.getPlayer().sendMessage(ChatColor.RED + "You cannot use that command while playing Assassin!");
					e.getPlayer().sendMessage(controller.formatMessage("commands.cannot-use"));
				}
			}
		}
	}

	public boolean entityIsPlayer(Entity e){
		return e.getType().equals(EntityType.PLAYER);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		if(controller.playerIsPlayingAssassin(e.getPlayer())) {
			if(e.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
				if(controller.getGameControl().isCurrentlyInProgress() && controller.getGameControl().isDeadlyLiquidEnabled()){
				if (e.getTo().getBlock().getType().equals(Material.WATER) ||
						e.getTo().getBlock().getType().equals(Material.STATIONARY_WATER) ||
						e.getTo().getBlock().getType().equals(Material.STATIONARY_LAVA) ||
						e.getTo().getBlock().getType().equals(Material.LAVA)) {
					killPlayer(e.getPlayer(), null);
				}
				}
			}
		}

		if(controller.getGameControl().isCurrentlyInProgress() && controller.playerIsPlayingAssassin(e.getPlayer())) {
			if(controller.getMapsConfig().mapIsProtected(controller.getGameControl().getCurrentMap())) {
				MapProtection protection = new MapProtection(controller.getMapsConfig(), controller.getGameControl().getCurrentMap());
				if (protection.restrictPlayers()) {
					if (!protection.locationIsInProtectedArea(e.getTo())) {
						e.getPlayer().sendMessage(controller.formatMessage("protection.protected"));
						e.setCancelled(false);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		if(e.getClickedBlock() != null){
			if(e.getClickedBlock().getType().equals(Material.BED)){
				if(controller.playerIsPlayingAssassin(e.getPlayer())){
					e.setCancelled(true);
					return;
				}
			}
		}

		if(e.getPlayer().hasPermission("assassin.op")){
			if(e.getItem() != null){
				if(e.getItem().getType().equals(Material.BLAZE_ROD)){
					if(e.getItem().hasItemMeta() && e.getItem().getItemMeta().getDisplayName().startsWith(ChatColor.AQUA + "" + ChatColor.BOLD)
							&& e.getItem().getItemMeta().getDisplayName().endsWith(" Protection Tool")){
						String map = e.getItem().getItemMeta().getDisplayName().split(" ")[0];
						if(map.length() > 8){
							map = map.substring(4,map.length() - 4);
						}

						if(!controller.getMapsConfig().hasMap(map)){
							e.getPlayer().sendMessage(ChatColor.RED + "Map '" + map + "' does not exist.");
							e.setCancelled(true);
							return;
						}

						if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
							new MapProtection(controller.getMapsConfig(),map).setLocation1(e.getClickedBlock().getLocation());
							e.getPlayer().sendMessage(ChatColor.GRAY + "Location 1 set!");
							e.setCancelled(true);
							return;
						}

						if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
							new MapProtection(controller.getMapsConfig(),map).setLocation2(e.getClickedBlock().getLocation());
							e.getPlayer().sendMessage(ChatColor.GRAY + "Location 2 set!");
							e.setCancelled(true);
							return;
						}
					}
				}
			}
		}

		if(!e.getPlayer().hasPermission("assassin.op")) {
			if (e.getClickedBlock() != null) {
				for (MapProtection protection : controller.getMapsConfig().getMapsProtections()) {
					if (!protection.allowInteraction()) {
						if (protection.locationIsInProtectedArea(e.getClickedBlock().getLocation())) {
							e.getPlayer().sendMessage(controller.formatMessage("protection.protected"));
							e.setCancelled(false);
							return;
						}
					}
				}
			}
		}

		if(e.getAction().equals(Action.LEFT_CLICK_AIR)||e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
			if(controller.playerIsPlayingAssassin(e.getPlayer())){
				if(e.getItem() != null && e.getItem().getType().equals(Material.IRON_SWORD)){
					if(controller.getGameControl().getCooldownPlayers().contains(e.getPlayer().getName())){
						e.setCancelled(true);
					}else{
						final int cooldownTime = controller.getMainConfig().getSwordCooldown() * 20;
						if(cooldownTime > 0){
							new CooldownRunnable(cooldownTime,e.getPlayer(),controller).runTaskTimer(controller,1,1);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent e){
		for(MapProtection protection : controller.getMapsConfig().getMapsProtections()){
			if(!protection.allowMobSpawn()){
				if(protection.locationIsInProtectedArea(e.getLocation())){
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		if(!e.getPlayer().hasPermission("assassin.op")) {
			for (MapProtection protection : controller.getMapsConfig().getMapsProtections()) {
				if (!protection.allowWorldModification()) {
					if (protection.locationIsInProtectedArea(e.getBlock().getLocation())) {
						e.getPlayer().sendMessage(controller.formatMessage("protection.protected"));
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e){
		if(!e.getPlayer().hasPermission("assassin.op")) {
			for (MapProtection protection : controller.getMapsConfig().getMapsProtections()) {
				if (!protection.allowWorldModification()) {
					if (protection.locationIsInProtectedArea(e.getBlock().getLocation())) {
						e.getPlayer().sendMessage(controller.formatMessage("protection.protected"));
						e.setCancelled(true);
					}
				}
			}
		}
	}
}
