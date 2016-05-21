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
import sun.util.resources.cldr.zh.CalendarData_zh_Hans_CN;

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

		if (args.length > 0){
			if(args[0].equalsIgnoreCase("config")){
				return onCommandConfig(p,Arrays.copyOfRange(args,1,args.length));
			}else if(args[0].equalsIgnoreCase("map")){
				if(args.length > 1 && controller.currentCoordinator != null && controller.currentCoordinator.getName().equals(p.getName())){
					if(controller.getMainConfig().hasMap(args[1])){
						controller.getGameControl().setCurrentMap(args[1]);
					}else{
						p.sendMessage(ChatColor.RED + "Map '" + args[1] + "' does not exist.");
					}
				}else if(args.length > 2){
					p.sendMessage(ChatColor.RED + "You must be the game coordinator to choose the map.");
				}else{
					if(controller.getGameControl().getCurrentMap().equals("")){
						p.sendMessage(ChatColor.AQUA + "There is no current map.");
					}
					p.sendMessage(ChatColor.AQUA + "The current map is '" + controller.getGameControl().getCurrentMap() + "'.");
				}
				return true;
			}
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
			}else if(args[0].equalsIgnoreCase("maps")){
				String[] maps = controller.getMainConfig().getMaps().toArray(new String[]{});
				if(maps.length > 0) {
					StringBuilder message = new StringBuilder(ChatColor.AQUA + "");
					for (int i = 0; i < maps.length - 1; i++) {
						message.append(maps[i]).append(", ");
					}
					message.append(maps[maps.length - 1]);
					p.sendMessage(message.toString());
				}else{
					p.sendMessage(ChatColor.AQUA + "There are no configured maps.");
				}
				return true;
			}
		}

		if(!controller.playerIsPlayingAssassin(p)){
			p.sendMessage(ChatColor.RED + "You must be playing Assassin.");
			return true;
		}

		if(controller.currentCoordinator == null){
			p.sendMessage(ChatColor.RED + "Error: No Current Coordinator");
			return true;
		}


		if(args.length == 1){
			if(args[0].equalsIgnoreCase("start")){
				controller.getGameControl().startCountdown(p);
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

	private boolean onCommandConfig(Player p, String[] args){
		if (args.length < 1){
			return false;
		}

		if(args[0].equalsIgnoreCase("spawn")){
			if(args.length < 2){
				return false;
			}

			if(args[1].equalsIgnoreCase("lobby")){
				controller.getMainConfig().setLobbySpawn(p.getLocation());
				p.sendMessage(ChatColor.GRAY + "Lobby Spawn set!");
				return true;
			}

			if(controller.getMainConfig().hasMap(args[1])){
				controller.getMainConfig().setMapSpawn(args[1],p.getLocation());
				p.sendMessage(ChatColor.GRAY + "Spawn set for map " + args[1] + ".");
			}else{
				p.sendMessage(ChatColor.RED + "Map '" + args[1] + "' does not exist.");
			}
		}else if(args[0].equalsIgnoreCase("map")){
			if(args.length < 3){
				return false;
			}

			if(args[1].equalsIgnoreCase("create")){
				if(args[2].equalsIgnoreCase("lobby")){
					p.sendMessage(ChatColor.RED + "You cannot create a map named 'lobby'.");
					return true;
				}

				if(controller.getMainConfig().hasMap(args[2])){
					p.sendMessage(ChatColor.RED + "Map '" + args[2] + "' already exists.");
					return true;
				}

				controller.getMainConfig().setMapSpawn(args[2],p.getLocation());
				p.sendMessage(ChatColor.GRAY + "Map '" + args[2] + "' has been created and its spawn has been set.");
			}else if(args[1].equalsIgnoreCase("delete")){
				if(controller.getMainConfig().hasMap(args[2])){
					controller.getMainConfig().removeMap(args[2]);
					p.sendMessage(ChatColor.GRAY + "Map '" + args[2] + "' has been deleted.");
				}else{
					p.sendMessage(ChatColor.RED + "Map '" + args[2] + "' does not exist.");
				}
			}
		}

		return true;
	}
}
