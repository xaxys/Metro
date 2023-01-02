package com.github.xaxys.metro;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandHandler implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SuccessfulHandle:
        {
            if (args.length == 0) break SuccessfulHandle;
            if (args[0].equalsIgnoreCase("loop")) {

                if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(Main.PERM_CREATE)) {
                    sender.sendMessage(Conf.MSG_NOPERM);
                    break SuccessfulHandle;
                }

                if (args.length == 3) {

                    // switch a line to loop line/straight line

                    boolean isLoop = false;
                    if (args[2].equalsIgnoreCase("t") || args[2].equalsIgnoreCase("true")) {
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

                if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(Main.PERM_SPEED)) {
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

                if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(Main.PERM_RELOAD)) {
                    sender.sendMessage(Conf.MSG_NOPERM);
                    break SuccessfulHandle;
                }

                // reload configuration

                Conf.loadConfig();
                sender.sendMessage("§aMetro Plugin Reloaded!");
            } else if (args[0].equalsIgnoreCase("debug")) {

                if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(Main.PERM_RELOAD)) {
                    sender.sendMessage(Conf.MSG_NOPERM);
                    break SuccessfulHandle;
                }

                // enable/disable debug mode

                Conf.DEBUG = !Conf.DEBUG;
                Conf.config.set("debug", Conf.DEBUG);
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            if (sender.hasPermission(Main.PERM_CREATE)) {
                subCommands.add("loop");
            }
            if (sender.hasPermission(Main.PERM_SPEED)) {
                subCommands.add("speed");
            }
            if (sender.hasPermission(Main.PERM_RELOAD)) {
                subCommands.add("reload");
                subCommands.add("debug");
            }
            return subCommands;
        } else if (args.length == 2) {
            if (args[0].equals("loop") || args[0].equals("speed")) {
                return new ArrayList<>(DataBase.DB.MetroMap.keySet());
            }
        } else if (args.length == 3) {
            if (args[0].equals("loop")) {
                return List.of("true", "false");
            } else if (args[0].equals("speed")) {
                return List.of("[double value]");
            }
        }
        return Collections.emptyList();
    }
}
