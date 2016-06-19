/*
 * Copyright (c) 2015. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import sun.util.resources.cldr.zh.CalendarData_zh_Hans_CN;

import java.util.*;

/**
 * Handles the execution of /assassin and the command's tab completer.
 *
 * Command structure:
 *
 * - assassin
 *   - config
 *     - spawn
 *       - lobby
 *       - {map}
 *     - map (or maps)
 *       - create (or add)
 *       - delete (or delete)
 *       - protect
 *   - help
 *   - join
 *   - leave
 *   - leaderboards
 *   - maps
 *   - map
 *     - {map}
 */
public class AssassinCommand implements CommandExecutor, TabCompleter {

	AssassinMinigame controller;

	public AssassinCommand(AssassinMinigame controller) {
		this.controller = controller;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player){
			if(!executeCommand((Player)sender, args)){
				sender.sendMessage(controller.formatMessage("commands.invalid-syntax"));
			}

		}else{
			sender.sendMessage(controller.formatMessage("commands.must-be-player"));
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
				if(args.length > 1 &&
						(p.hasPermission("assassin.op") || (controller.currentCoordinator != null && controller.currentCoordinator.getName().equals(p.getName())))){
					if(controller.getGameControl().preGameCountdownStarted || controller.getGameControl().isCurrentlyInProgress()){
						p.sendMessage("map.during-game");
						return true;
					}
					if(controller.getMapsConfig().hasMap(args[1])){
						controller.getGameControl().setCurrentMap(args[1]);
					}else{
//						p.sendMessage(ChatColor.RED + "Map '" + args[1] + "' does not exist.");
						p.sendMessage(controller.formatMessage("map.does-not-exist","%map",args[1]));
					}
				}else if(args.length > 2){
					p.sendMessage(controller.formatMessage("commands.not-coordinator"));
				}else{
					if(controller.getGameControl().getCurrentMap().equals("")){
						p.sendMessage(controller.formatMessage("map.no-current-map"));
						return true;
					}
//					p.sendMessage(ChatColor.AQUA + "The current map is '" + controller.getGameControl().getCurrentMap() + "'.");
					p.sendMessage(controller.formatMessage("map.current-map","%map",controller.getGameControl().getCurrentMap()));
				}
				return true;
			}else if(args[0].equalsIgnoreCase("tp")){
				if(args.length > 1 && p.hasPermission("assassin.op")){
					if(args[1].equalsIgnoreCase("lobby")){
						p.teleport(controller.getMainConfig().getLobbySpawn());
					}else if(controller.getMapsConfig().hasMap(args[1])){
						p.teleport(controller.getMapsConfig().getMapSpawn(args[1]));
					}else{
						p.sendMessage(ChatColor.RED + "That map does not exist.");
					}
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
					p.sendMessage(controller.formatMessage("game.already-playing"));
				}
				return true;
			}else if(args[0].equalsIgnoreCase("leave")){
				if(controller.removePlayerFromGame(p)){
					p.sendMessage(controller.formatMessage("game.not-playing"));
				}
				return true;
			}else if(args[0].equalsIgnoreCase("leaderboards")){
				//p.sendMessage(ChatColor.RED + "This command is not ready yet!");
				List<OfflinePlayer> top5 = getTopFivePlayersFromScoreboard();
				Scoreboard sb = controller.getServer().getScoreboardManager().getMainScoreboard();
				Objective ob = sb.getObjective("assassinScore");
				p.sendMessage(controller.formatMessage("leaderboard.header"));
				for(OfflinePlayer player:top5){
//					p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + player.getName() + ChatColor.RESET + ": " +
//							ob.getScore(player).getScore() + " points");
					p.sendMessage(controller.formatMessage("leaderboard.element",
							"%player",player.getName(),"%points",ob.getScore(player).getScore() + ""));
				}

				if(!top5.contains(p)){
					p.sendMessage(controller.formatMessage("leaderboard.divider"));
//					p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + p.getName() + ChatColor.RESET + ": " +
//							ob.getScore(p).getScore() + " points");
					p.sendMessage(controller.formatMessage("leaderboard.element",
							"%player",p.getName(),"%points",ob.getScore(p).getScore() + ""));
				}

				p.sendMessage(controller.formatMessage("leaderboard.footer"));
				return true;
			}else if(args[0].equalsIgnoreCase("maps")){
				String[] maps = controller.getMapsConfig().getMaps().toArray(new String[]{});
				if(maps.length > 0) {
					StringBuilder message = new StringBuilder(ChatColor.AQUA + "");
					for (int i = 0; i < maps.length - 1; i++) {
						message.append(maps[i]).append(", ");
					}
					message.append(maps[maps.length - 1]);
					p.sendMessage(message.toString());
				}else{
					p.sendMessage(controller.formatMessage("map.no-maps"));
				}
				return true;
			}
		}

		if(!controller.playerIsPlayingAssassin(p)){
			p.sendMessage(controller.formatMessage("game.must-be-playing"));
			return true;
		}

		if(args.length == 1){
			if(args[0].equalsIgnoreCase("start")){
				controller.getGameControl().startCountdown(p);
				return true;
			}else if (args[0].equalsIgnoreCase("end")){
				if(p.hasPermission("assassin.op")){
					controller.getGameControl().endGame(-1);
				}else{
					p.sendMessage(controller.formatMessage("commands.no-permission"));
				}
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
		List<String> messages = new ArrayList<>();

		messages.add(controller.formatMessage("commands.help.header"));

		if(p.hasPermission("assassin.op")){
			messages.add(controller.formatMessage("commands.help.config","%cmd","assassin config"));
		}

		messages.add(controller.formatMessage("commands.help.config","%cmd","assassin config"));
		messages.add(controller.formatMessage("commands.help.join","%cmd","assassin join"));
		messages.add(controller.formatMessage("commands.help.leave","%cmd","assassin leave"));
		messages.add(controller.formatMessage("commands.help.leaderboards","%cmd","assassin leaderboards"));
		messages.add(controller.formatMessage("commands.help.map","%cmd","assassin map"));
		messages.add(controller.formatMessage("commands.help.maps","%cmd","assassin maps"));
		messages.add(controller.formatMessage("commands.help.start","%cmd","assassin start"));

		if(p.hasPermission("assassin.op")){
			messages.add(controller.formatMessage("commands.help.tp","%cmd","assassin tp"));
		}

		p.sendMessage(messages.toArray(new String[]{}));
	}

	private boolean onCommandConfig(Player p, String[] args){
		if(!p.hasPermission("assassin.op")){
			p.sendMessage(controller.formatMessage("commands.no-permission"));
			return true;
		}

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

			if(controller.getMapsConfig().hasMap(args[1])){
				controller.getMapsConfig().setMapSpawn(args[1],p.getLocation());
				p.sendMessage(ChatColor.GRAY + "Spawn set for map " + args[1] + ".");
			}else{
				p.sendMessage(ChatColor.RED + "Map '" + args[1] + "' does not exist.");
			}
		}else if(args[0].equalsIgnoreCase("reload")){
			controller.reloadConfigs();
			p.sendMessage(ChatColor.GRAY + "Configuration files have been reloaded.");
		}else if(args[0].equalsIgnoreCase("map") || args[0].equalsIgnoreCase("maps")){
			if(args.length < 3){
				return false;
			}

			if(args[1].equalsIgnoreCase("create") || args[1].equalsIgnoreCase("add")){
				if(args[2].equalsIgnoreCase("lobby")){
					p.sendMessage(ChatColor.RED + "You cannot create a map named 'lobby'.");
					return true;
				}

				if(controller.getMapsConfig().hasMap(args[2])){
					p.sendMessage(ChatColor.RED + "Map '" + args[2] + "' already exists.");
					return true;
				}

				controller.getMapsConfig().setMapSpawn(args[2],p.getLocation());
				p.sendMessage(ChatColor.GRAY + "Map '" + args[2] + "' has been created and its spawn has been set.");
			}else if(args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("remove")){
				if(controller.getMapsConfig().hasMap(args[2])){
					controller.getMapsConfig().removeMap(args[2]);
					p.sendMessage(ChatColor.GRAY + "Map '" + args[2] + "' has been deleted.");
				}else{
					p.sendMessage(ChatColor.RED + "Map '" + args[2] + "' does not exist.");
				}
			}else if(args[1].equalsIgnoreCase("protect")){
				if(!controller.getMapsConfig().hasMap(args[2])){
					p.sendMessage(ChatColor.RED + "Map '" + args[2] + "' does not exist.");
					return true;
				}
				p.sendMessage(ChatColor.GRAY + "A selection tool has been placed in your inventory. Left click with it to select the first corner, and then right click with it to select the other corner.");
				ItemStack selectionTool = new ItemStack(Material.BLAZE_ROD);
				ItemMeta itemMeta = selectionTool.getItemMeta();
				itemMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + args[2] + ChatColor.RESET + "" + ChatColor.AQUA + " Protection Tool");
				itemMeta.setLore(Arrays.asList(ChatColor.AQUA + "Use this to protect '" + args[2] + "'."));
				selectionTool.setItemMeta(itemMeta);
				p.getInventory().addItem(selectionTool);
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
		List<String> completions = new ArrayList<>();

		if(args.length <= 1){
			if(sender.hasPermission("assassin.op")) {
				completions.add("config");
				completions.add("tp");
			}

			completions.add("help");
			completions.add("join");
			completions.add("leaderboards");
			completions.add("leave");
			completions.add("map");
			completions.add("maps");

			if(args.length == 1){
				return removeCompletions(completions,args[0]);
			}

			return completions;
		}

		if(args.length == 2 && args[0].equalsIgnoreCase("map")){
			completions.addAll(controller.getMapsConfig().getMaps());
			return removeCompletions(completions,args[1]);
		}

		if(args.length == 2 && args[0].equalsIgnoreCase("tp")){
			completions.add("lobby");
			completions.addAll(controller.getMapsConfig().getMaps());
			return removeCompletions(completions,args[1]);
		}

		if(args.length == 2 && args[0].equalsIgnoreCase("config")){
			if(sender.hasPermission("assassin.op")) {
				completions.add("spawn");
				completions.add("map");
				completions.add("reload");


				return removeCompletions(completions, args[1]);
			}
		}

		if(args.length == 3 && args[0].equalsIgnoreCase("config")){
			if(args[1].equalsIgnoreCase("spawn")){
				completions.add("lobby");
				completions.addAll(controller.getMapsConfig().getMaps());
				return removeCompletions(completions,args[2]);
			}else if(args[1].equalsIgnoreCase("map")||args[1].equalsIgnoreCase("maps")){
				completions.add("create");
				completions.add("delete");
				completions.add("protect");
				return removeCompletions(completions,args[2]);
			}
		}

		if(args.length == 4 && args[0].equalsIgnoreCase("config") && args[1].equalsIgnoreCase("map")){
			completions.addAll(controller.getMapsConfig().getMaps());
			return removeCompletions(completions,args[3]);
		}

		return null;
	}

	private static List<String> removeCompletions(List<String> completions, String startsWith){
		List<String> newCompletions = new ArrayList<>(completions);
		Collections.sort(newCompletions);
		for (String completion : completions) {
			if(!completion.toLowerCase().startsWith(startsWith.toLowerCase())){
				newCompletions.remove(completion);
			}
		}
		return newCompletions;
	}
}
