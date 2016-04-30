/*
 * Copyright (c) 2015. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

/**
 * Created by jasper on 9/15/15.
 */
public class AssassinCommand implements CommandExecutor {

	AssassinMinigame controller;

	public AssassinCommand(AssassinMinigame controller) {
		this.controller = controller;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player){
			if(!executeCommand((Player)sender, args)){
				sender.sendMessage(ChatColor.RED + "Invalid syntax. Type \"/assassin help\" for help.");
			}

		}else{
			sender.sendMessage(ChatColor.RED + "You must be a player!");
		}

		return true;
	}

	public boolean executeCommand(Player p,String[] args){
		if (args.length == 0 ){
			return false;
		}

		if(args.length == 1){
			if(args[0].equalsIgnoreCase("help")){
				sendHelp(p);
				return true;
			}
			if(args[0].equalsIgnoreCase("join")){
				if(controller.addPlayerToGame(p)){
					p.sendMessage(ChatColor.RED + "You are already playing Assassin.");
				}
				return true;
			}else if(args[0].equalsIgnoreCase("leave")){
				if(controller.removePlayerFromGame(p)){
					p.sendMessage(ChatColor.RED + "You are not playing Assassin.");
				}
				return true;
			}else if(args[0].equalsIgnoreCase("leaderboards")){
				//p.sendMessage(ChatColor.RED + "This command is not ready yet!");
				List<OfflinePlayer> top5 = getTopFivePlayersFromScoreboard();
				Scoreboard sb = controller.getServer().getScoreboardManager().getMainScoreboard();
				Objective ob = sb.getObjective("assassinScore");
				p.sendMessage("------------ " + ChatColor.AQUA + "Top 5 Scores For Assassin" + ChatColor.RESET + " ------------");
				for(OfflinePlayer player:top5){
					p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + player.getName() + ChatColor.RESET + ": " +
							ob.getScore(player).getScore() + " points");
				}

				if(!top5.contains(p)){
					p.sendMessage(" ................");
					p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + p.getName() + ChatColor.RESET + ": " +
							ob.getScore(p).getScore() + " points");
				}

				p.sendMessage("-------------------------------------------------");
				return true;
			}else if(args[0].contains("spawn")){
				if(p.hasPermission("assassin.op")){
					controller.setSpawn(p.getLocation());
					p.sendMessage(ChatColor.GRAY + "Spawn set!");
				}else{
					p.sendMessage(ChatColor.RED + "You do not have permission.");
				}
				return true;
			}
		}

		if(!controller.playerIsPlayingAssassin(p)){
			p.sendMessage(ChatColor.RED + "You must be playing Assassin.");
			return true;
		}

		if(args.length == 1){
			if(args[0].equalsIgnoreCase("start")){
				if(controller.currentCoordinator.equals(p)||p.hasPermission("assassin.op")){
					controller.getGameControl().startCountdown(p);
				}else{
					p.sendMessage(ChatColor.RED + "You must be the game coordinator to use this command.");
				}
				return true;
			}
		}

		return false;
	}

	public List<OfflinePlayer> getTopFivePlayersFromScoreboard(){
		Scoreboard sb = controller.getServer().getScoreboardManager().getMainScoreboard();
		Objective ob = sb.getObjective("assassinScore");
		List<OfflinePlayer> allPlayers = Arrays.asList(controller.getServer().getOfflinePlayers()); //new ArrayList<>(controller.getServer().getOfflinePlayers());
		allPlayers.sort((o1, o2) -> {
			if(ob.getScore(o1).getScore() < ob.getScore(o2).getScore()){
				return 1;
			}else{
				return -1;
			}
		});

		if(allPlayers.size() > 5) {
			return allPlayers.subList(0, 5);
		}else{
			return  allPlayers;
		}


	}

	public void sendHelp(Player p){
		String[] message = {"------------ " + ChatColor.GOLD + "Assassin Minigame Help" + ChatColor.RESET + " ------------",
		                    ChatColor.GOLD + "/assassin join" + ChatColor.RESET + ": Join the minigame",
							ChatColor.GOLD + "/assassin leave" + ChatColor.RESET + ": Leave the minigame",
							ChatColor.GOLD + "/assassin leaderboards" + ChatColor.RESET + ": Show the top 5 players in Assassin", "   this month",
							ChatColor.GOLD + "/assassin start" + ChatColor.RESET + ": Start the game"};
		p.sendMessage(message);
	}
}
