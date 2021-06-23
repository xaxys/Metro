package com.github.xaxys.metro;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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
		Bukkit.getConsoleSender().sendMessage(Conf.MSG_DBG + "§bConfiguraion Loaded!");

		DataBase.DB = DataBase.readDB(Conf.DB_PATH);
		Bukkit.getConsoleSender().sendMessage(Conf.MSG_DBG+String.format("§b%d Lines and %d Stations Loaded!",
				DataBase.DB.MetroMap.size(), DataBase.DB.PositionMap.size()));

		getServer().getPluginManager().registerEvents(new EventListener(), this);
		Bukkit.getConsoleSender().sendMessage(Conf.MSG_DBG + "§bEventListener Registered!");

		Bukkit.getConsoleSender().sendMessage(Conf.MSG_DBG + "§bMetro Plugin Loaded!");
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
			SuccessfulHandle:
			{
				if (args.length == 0) break SuccessfulHandle;
				if (args[0].equalsIgnoreCase("loop")) {

					if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(PERM_CREATE)) {
						sender.sendMessage(Conf.MSG_NOPERM);
						break SuccessfulHandle;
					}

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
						break SuccessfulHandle;
					}
				} else if (args[0].equalsIgnoreCase("speed")) {

					if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(PERM_CREATE)) {
						sender.sendMessage(Conf.MSG_NOPERM);
						break SuccessfulHandle;
					}

					if (args.length == 3) {

						// set speed of line

						double speed = Conf.NORMAL_SPEED;
						try {
							speed = Double.parseDouble(args[2]);
						} catch (NumberFormatException e) { }

						if (DataBase.DB.setSpeed(args[1], speed)) {
							sender.sendMessage(Conf.MSG_DBG + "MetroLine " + args[1] + " speed set to " + speed);
						} else {
							sender.sendMessage(Conf.MSG_DBG + "MetroLine " + args[1] + " not found");
						}
					} else if (args.length == 2) {

						// display speed of line

						Double speed = DataBase.DB.getSpeed(args[1]);
						if (speed != null) {
							sender.sendMessage(Conf.MSG_DBG + "MetroLine " + args[1] + " speed is " + speed);
						} else {
							sender.sendMessage(Conf.MSG_DBG + "MetroLine " + args[1] + " not found");
						}
					} else {
						break SuccessfulHandle;
					}
				} else if (args[0].equalsIgnoreCase("reload")) {

					if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(PERM_RELOAD)) {
						sender.sendMessage(Conf.MSG_NOPERM);
						break SuccessfulHandle;
					}

					// reload configuration

					Conf.loadConfig();
					sender.sendMessage("§aMetro Plugin Reloaded!");
				} else if (args[0].equalsIgnoreCase("debug")) {

					if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(PERM_RELOAD)) {
						sender.sendMessage(Conf.MSG_NOPERM);
						break SuccessfulHandle;
					}

					// enable/disable debug mode

					Conf.DEBUG = !Conf.DEBUG;
					sender.sendMessage("§aMetro Plugin DebugMode:" + Conf.DEBUG);
				}
				return true;
			}

			UnsuccessfulHandle:
			{
				sender.sendMessage(Conf.MSG_USAGE);
				return true;
			}
		}
		return false;
	}

}
