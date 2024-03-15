package com.action35.findtheitem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players may use this command!");
		}

		Player player = (Player) sender;

		if (command.getName().equalsIgnoreCase("join")) {
			if (!Main.playersDone.containsKey(player.getUniqueId())) {
				Main.playersDone.put(player.getUniqueId(), false);
				Main.timerBar.addPlayer(player);
			} else {
				player.sendMessage(Utils.chat("&c" + "You are already playing!"));
			}
		}

		if (command.getName().equalsIgnoreCase("leave")) {
			if (Main.playersDone.containsKey(player.getUniqueId())) {
				Main.playersDone.remove(player.getUniqueId());
				Main.timerBar.removePlayer(player);
			} else {
				player.sendMessage(Utils.chat("&c" + "You are not currently playing!"));
			}
		}

		if (command.getName().equalsIgnoreCase("startgame")) {
			if (player.isOp()) {
				if (args.length > 0) {
					try {
						Main.gameTime = Integer.parseInt(args[0]);
					} catch (NumberFormatException ex) {
						player.sendMessage(Utils.chat("&c" + args[0] + " is not a valid number!"));
					}
				}
				
				Main.timer = 5;
				Main.gameState = "Playing";
				player.sendMessage(Utils.chat("&aGame started with " + Main.playersDone.size() + " players!"));
				Main.reset();
			}
		}

		if (command.getName().equalsIgnoreCase("stopgame")) {
			if (player.isOp()) {
				Main.gameState = "Not Started";
				player.sendMessage(Utils.chat("&aGame Stopped!"));
			}
		}

		return true;
	}
}
