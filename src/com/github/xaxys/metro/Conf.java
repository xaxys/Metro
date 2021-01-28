package com.github.xaxys.metro;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class Conf {
	// Global Variables:
	public static BukkitTask CLTMR = null;
	public static final Object API_SYNC = new Object();

	// Global Config Settings:
	public static boolean DEBUG = false;
	public static String TITLE;
	public static String ERROR;
	public static String L_ST;
	public static String L_END;

	// Constants:
	public static final String MSG_DBG = "§e[Metro] §r";
	public static final String MSG_ERR_ST = "§e[Metro] §eError in §b";
	public static final String MSG_ERR_MID = "§e: §c";
	public static final String CONFIG_PATH = "plugins/Metro/config.yml";
	public static final String DB_PATH = "plugins/Metro/db.bin";
	
	public static MemoryConfiguration defaults = new MemoryConfiguration();
	public static Configuration config = null;

	public static void initDefaults() {
		defaults.set("debug", false);
		defaults.set("title", "&a[&3&lMetro&aS]");
		defaults.set("error", "[&4Wrong Metro Format&r]");
		defaults.set("selStart", "&8> &5");
		defaults.set("selEnd", " &8<");
	}

	public static void loadConfig() {
		try {
			File path = new File(CONFIG_PATH);
			if (path.exists()) {
				config = Main.plugin.getConfig();
			} else {
				Main.plugin.saveDefaultConfig();
				config = Main.plugin.getConfig();
			}

			config.setDefaults(defaults);
			CLTMR = null;

			// Load Global Settings:
			DEBUG = config.getBoolean("debug");
			TITLE = c(config.getString("title"));
			ERROR = c(config.getString("error"));
			L_ST = c(config.getString("selStart"));
			L_END = c(config.getString("selEnd"));

		} catch (Exception e) {
			err("loadConfig", "Caught Exception: " + e.getMessage());
		}
	}

	public static void dbg(String str) {
		if (DEBUG) {
			String msg = MSG_DBG + str;
			Bukkit.getConsoleSender().sendMessage(msg);
			Bukkit.getOnlinePlayers().forEach((p) -> {
				if (p.hasPermission(Main.PERM_RELOAD)) {
					p.sendMessage(msg);
				}
			});
		}
	}

	public static void err(String func, String cause) {
		String msg = MSG_ERR_ST + func + MSG_ERR_MID + cause;
		Bukkit.getConsoleSender().sendMessage(msg);
		if (DEBUG) {
			Bukkit.getOnlinePlayers().forEach((p) -> {
				if (p.hasPermission(Main.PERM_RELOAD)) {
					p.sendMessage(msg);
				}
			});
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