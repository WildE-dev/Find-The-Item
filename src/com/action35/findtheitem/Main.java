package com.action35.findtheitem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	public static Map<UUID, Boolean> playersDone = new HashMap<UUID, Boolean>();
	public static Material itemToGet = Material.AIR;
	public static String gameState = "Not Started";
	public static int timer = 0;

	public static int gameTime = 600;

	public static BossBar timerBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);

	private static List<Material> possibleItems = new ArrayList<Material>();

	@Override
	public void onEnable() {
		saveDefaultConfig();
		for (String val : getConfig().getStringList("items")) {
			possibleItems.add(Material.valueOf(val.toUpperCase()));

		}

		Commands cmd = new Commands();
		getCommand("join").setExecutor(cmd);
		getCommand("leave").setExecutor(cmd);
		getCommand("startgame").setExecutor(cmd);
		getCommand("stopgame").setExecutor(cmd);
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getConsoleSender().sendMessage("[FindTheItem] Plugin is enabled!");
		
		timer();
	}
	
	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage("[FindTheItem] Plugin is disabled!");
	}

	private void timer() {
		getServer().getScheduler().runTaskTimer(this, new Runnable() {

			public void run() {
				if (gameState.equals("Playing")) {
					timerBar.setColor(BarColor.RED);
					timerBar.setTitle(Utils.chat("&c&l" + itemName() + " &f|&c " + getFormattedTimer()));
					timerBar.setProgress((double) timer / gameTime);
					timer--;
					if (timer <= 0) {
						timer = 5;

						for (Map.Entry<UUID, Boolean> entry : playersDone.entrySet()) {
							UUID key = entry.getKey();
							boolean value = entry.getValue();
							Player p = Bukkit.getPlayer(key);

							if (!value) {
								p.setHealth(0D);
								Bukkit.broadcastMessage(
										Utils.chat("&c" + p.getDisplayName() + " did not find the item!"));
							}

							entry.setValue(false);
						}

						reset();
					}
				} else {
					timerBar.setColor(BarColor.WHITE);
					timerBar.setTitle(Utils.chat("Waiting to Start..."));
					timerBar.setProgress(1);
				}
			}
		}, 0L, 20L);
	}

	private String getFormattedTimer() {
		int mins = timer / 60;
		int secs = timer % 60;
		String minsS = mins + "";
		String secsS = secs + "";
		
		if(mins < 10)
			minsS = "0" + mins;
		if (secs < 10)
			secsS = "0" + secs;
		
		return minsS + ":" + secsS;
	}
	
	public static void reset() {
		timer = gameTime;
		itemToGet = possibleItems.get(new Random().nextInt(possibleItems.size()));

		Bukkit.broadcastMessage(Utils.chat("&eYou must find &f&l" + itemName()));
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 1f);
		}
	}

	private static String itemName() {
		return Utils.toTitleCase(itemToGet.name().replace('_', ' ').toLowerCase());
	}

	@EventHandler
	private void onPlayerDropItem(PlayerDropItemEvent event) {		
		Player player = event.getPlayer();

		if (!playersDone.get(player.getUniqueId())) {
			if (event.getItemDrop().getItemStack().getType().equals(itemToGet)) {
				event.setCancelled(true);
				player.getInventory().remove(new ItemStack(itemToGet, 1));
				playersDone.put(player.getUniqueId(), true);
				Bukkit.broadcastMessage(Utils.chat("&a" + player.getDisplayName() + " found the item!"));
				player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);

				if (allDone()) {
					timer = 0;
				}
			}
		}
	}

	private boolean allDone() {
		for (Map.Entry<UUID, Boolean> entry : playersDone.entrySet()) {
			boolean value = entry.getValue();

			if (!value)
				return false;
		}

		return true;
	}

	@EventHandler
	private void onPlayerLeave(PlayerQuitEvent event) {
		timerBar.removePlayer(event.getPlayer());
		playersDone.remove(event.getPlayer().getUniqueId());
	}
}
