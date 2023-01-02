package com.github.xaxys.metro;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements CommandExecutor {

	public static final String PERM_RELOAD = "metro.reload";
	public static final String PERM_CREATE = "metro.create";
	public static final String PERM_SPEED = "metro.speed";
	public static final String PERM_USE = "metro.use";
	public static Main plugin;

	@Override
	public void onEnable() {
		plugin = this;
		Conf.initDefaults();

		Conf.loadConfig();
		getServer().getConsoleSender().sendMessage(Conf.MSG_DBG + "§bConfiguration Loaded!");

		DataBase.DB = DataBase.readDB(Conf.DB_PATH);
		getServer().getConsoleSender().sendMessage(Conf.MSG_DBG + String.format("§b%d Lines and %d Stations Loaded!",
				DataBase.DB.MetroMap.size(), DataBase.DB.PositionMap.size()));

		getCommand("metro").setExecutor(new CommandHandler());
		getServer().getPluginManager().registerEvents(new EventListener(), this);
		getServer().getConsoleSender().sendMessage(Conf.MSG_DBG + "§bCommandHandler and EventListener Registered!");

		getServer().getConsoleSender().sendMessage(Conf.MSG_DBG + "§bMetro Plugin Loaded!");
	}
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll();
		DataBase.DB.saveDB(Conf.DB_PATH);
		saveConfig();
	}

}
