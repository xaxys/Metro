package com.github.xaxys.metro;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements CommandExecutor {
	
	public static Main plugin;
	public static final String PERM_RELOAD = "metro.reload";
	public static final String PERM_CREATE = "metro.create";
	public static final String PERM_USE = "metro.use";
	
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
			if (args[0].equalsIgnoreCase("loop") && sender.hasPermission(PERM_CREATE)) {
				if (args.length == 3) {

					// switch a line to loop line/straight line

					boolean isLoop = false;
					if (args[2].equalsIgnoreCase("t") || args[1].equalsIgnoreCase("true")) {
						isLoop = true;
					}
					if (DataBase.DB.setLoop(args[1], isLoop)) {
						sender.sendMessage(Conf.MSG_DBG + "MetroLine " + args[1] + " isLoop set to " + isLoop);
					} else {
						sender.sendMessage(Conf.MSG_DBG + "MetroLine " + args[1] + " not found");
					}
				} else if (args.length == 2) {

					// get loop status of line

					Boolean isLoop = DataBase.DB.getLoop(args[1]);
					if (isLoop != null) {
						sender.sendMessage(Conf.MSG_DBG + "MetroLine " + args[1] + " isLoop is " + isLoop);
					} else {
						sender.sendMessage(Conf.MSG_DBG + "MetroLine " + args[1] + " not found");
					}
				} else {
					sender.sendMessage(Conf.MSG_USAGE);
				}
			} else if(args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission(PERM_RELOAD)) {

				// reload configuration

				Conf.loadConfig();
				sender.sendMessage("§aMetro Plugin Reloaded!");
			} else if(args.length == 1 && args[0].equalsIgnoreCase("debug") && sender.hasPermission(PERM_RELOAD)) {

				// enable/disable debug mode

				Conf.DEBUG = !Conf.DEBUG;
				sender.sendMessage("§aMetro Plugin DebugMode:"+Conf.DEBUG);
			} else {
				sender.sendMessage(new String[]{
					"§cUsage: /metro reload",
					"§cUsage: /metro loop [true/false] [MetroLine Name]",
				});
			}
			return true;
		}
		return false;
	}

}
