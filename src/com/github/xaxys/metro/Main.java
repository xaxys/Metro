package com.github.xaxys.metro;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin implements Listener {
	
	public static Main plugin = null;
	public static final String PERM_RELOAD = "metro.reload";
	
	@Override
	public void onEnable() {
		plugin = this;
		Conf.initDefaults();
		Conf.loadConfig();
		DataBase.DB = DataBase.readDB(Conf.DB_PATH);
		getServer().getPluginManager().registerEvents(new EventListener(), this);
		Bukkit.getConsoleSender().sendMessage(Conf.MSG_DBG+"§bMetro Plugin Loaded!");
	}
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll();
		DataBase.DB.saveDB(Conf.DB_PATH);
		saveConfig();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("metro")) {
			if (args.length == 1 && args[0].equalsIgnoreCase("check") && sender.hasPermission(PERM_RELOAD)) {
				sender.sendMessage(DataBase.DB.toString());
			} else if(args.length == 1 && args[0].equalsIgnoreCase("debug") && sender.hasPermission(PERM_RELOAD)) {
				Conf.DEBUG = Conf.DEBUG == true ? false : true;
				sender.sendMessage("§aMetro Plugin DebugMode:"+Conf.DEBUG);
			} else if(args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission(PERM_RELOAD)) {
				setTimeout(() -> {
					Conf.loadConfig();
					sender.sendMessage("§aMetro Plugin Reloaded!");
				}, 200);
			} else {
				sender.sendMessage("§cUsage: /metro reload");
			}
			return true;
		}
		return false;
	}
	
	public BukkitTask setTimeout(Runnable function, long millis) {
		return new BukkitRunnable() {
			public void run() {
				synchronized (Conf.API_SYNC) {
					function.run();
				}
			}
		}.runTaskLater(this, millis / 50);
	}

	public BukkitTask setInterval(Runnable function, long millis) {
		long t = millis / 50;
		return new BukkitRunnable() {
			public void run() {
				synchronized (Conf.API_SYNC) {
					function.run();
				}
			}
		}.runTaskTimer(this, t, t);
	}

	public BukkitTask setInterval(BukkitRunnable function, long millis) {
		long t = millis / 50;
		return function.runTaskTimer(this, t, t);
	}

}
