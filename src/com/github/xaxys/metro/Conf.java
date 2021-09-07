package com.github.xaxys.metro;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Conf {

	// Global Config Settings:
	public static boolean DEBUG;
	public static String TITLE;
	public static String ERROR;
	public static String L_ST;
	public static String L_END;
	public static List<String> DISABLE_WORLDS;

	// Constants:
	public static final String MSG_DBG = "§e[Metro] §r";
	public static final String MSG_ERR_ST = "§e[Metro] §eError in §b";
	public static final String MSG_ERR_MID = "§e: §c";
	public static final String MSG_NOPERM = "§cYou don't have permission for this";
	public static final String CONFIG_PATH = "plugins/Metro/config.yml";
	public static final String DB_PATH = "plugins/Metro/db.bin";
	public static final int ADJUST_LENGTH = 20;
	public static final int BUFFER_LENGTH = 3;
	public static final double NORMAL_SPEED = 0.4;
	public static final String[] MSG_USAGE = new String[]{
			"§cUsage: /metro reload",
			"§cUsage: /metro loop [MetroLine Name] {[true|false]}",
			"§cUsage: /metro speed [MetroLine Name] {[double value]}",
	};
	
	public static MemoryConfiguration defaults = new MemoryConfiguration();
	public static Configuration config = null;

	public static void initDefaults() {
		defaults.set("debug", false);
		defaults.set("title", "&a[&3&lMetro&aS]");
		defaults.set("error", "[&4Wrong Metro Format&r]");
		defaults.set("selStart", "&8> &5");
		defaults.set("selEnd", " &8<");
		defaults.set("disabled_worlds", Arrays.asList("disabled_world"));
	}

	public static void loadConfig() {
		try {
			File path = new File(CONFIG_PATH);
			if (!path.exists()) {
				Main.plugin.saveDefaultConfig();
			}
			config = Main.plugin.getConfig();
			config.setDefaults(defaults);

			// Load Global Settings:
			DEBUG = config.getBoolean("debug");
			TITLE = c(config.getString("title"));
			ERROR = c(config.getString("error"));
			L_ST = c(config.getString("selStart"));
			L_END = c(config.getString("selEnd"));
			DISABLE_WORLDS = config.getStringList("disabled_worlds");
			
		} catch (Exception e) {
			err("loadConfig", "Caught Exception: " + e.getMessage());
		}
	}

	public static void dbg(String str) {
		if (DEBUG) {
			String msg = MSG_DBG + str;
			Bukkit.getConsoleSender().sendMessage(msg);
			Bukkit.getOnlinePlayers()
					.parallelStream()
					.filter(p -> p.hasPermission(Main.PERM_RELOAD))
					.forEach(p -> p.sendMessage(msg));
		}
	}

	public static void err(String func, String cause) {
		String msg = MSG_ERR_ST + func + MSG_ERR_MID + cause;
		Bukkit.getConsoleSender().sendMessage(msg);
		if (DEBUG) {
			Bukkit.getOnlinePlayers()
					.parallelStream()
					.filter(p -> p.hasPermission(Main.PERM_RELOAD))
					.forEach(p -> p.sendMessage(msg));
		}
	}
	
	public static void msg(Player p, String str) {
		String msg = MSG_DBG + str;
		p.sendMessage(msg);
	}

	public static String c(String s) {
		return s.replace("&", "§");
	}
}