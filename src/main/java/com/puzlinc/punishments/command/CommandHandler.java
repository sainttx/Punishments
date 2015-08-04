/*
* Punishments
* Copyright (C) 2014 Puzl Inc.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.puzlinc.punishments.command;

import com.puzlinc.punishments.PunishmentManager;
import com.puzlinc.punishments.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommandHandler implements CommandExecutor {

    private PunishmentManager manager;

    public CommandHandler(PunishmentManager manager) {
        this.manager = manager;
    }

    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
        String label = cmd.getLabel();
        UUID admin = (cs instanceof Player) ? ((Player) cs).getUniqueId() : null;

        if (label.equalsIgnoreCase("kick")) {
            if (cs.hasPermission("punishments.kick")) {
                if (args.length >= 1) {
                    String name = args[0];
                    Player player = Bukkit.getPlayer(name);

                    if (player == null) {
                        cs.sendMessage(ChatColor.RED + "Cannot find player: " + name);
                        return true;
                    }

                    String reason = null;
                    if (args.length >= 2) {
                        reason = Util.argsToString(args, 1, args.length);
                    }
                    PunishmentManager.Punishment punishment = manager.addPunishment(
                            PunishmentManager.PunishmentType.KICK,
                            player.getUniqueId(),
                            admin,
                            cs.getName(),
                            System.currentTimeMillis(),
                            manager.PUNISHMENT_EXPIRED,
                            manager.getServer(),
                            reason
                    );

                    player.kickPlayer(punishment.getMessage());
                    Bukkit.broadcastMessage(ChatColor.RED + "Player " + player.getName() + " was kicked by " + cs.getName());
                } else {
                    cs.sendMessage(ChatColor.RED + "Usage: /kick <player> [reason ...]");
                }
            } else {
                cs.sendMessage(ChatColor.RED + "Insufficient permissions.");
            }
        } else if (label.equalsIgnoreCase("ban")) {
            if (cs.hasPermission("punishments.ban")) {
                if (args.length >= 2) {
                    String name = args[0];
                    OfflinePlayer player = Bukkit.getOfflinePlayer(name);

                    if (player == null) {
                        cs.sendMessage(ChatColor.RED + "Cannot find player: " + name);
                        return true;
                    }

                    String reason = null;
                    long length = manager.PUNISHMENT_EXPIRE_NEVER;
                    long givenTime = Util.lengthToMiliseconds(args[1]);
                    if (givenTime != 0) {
                        length = System.currentTimeMillis() + givenTime;
                    }
                    if (args.length >= 3) {
                        reason = Util.argsToString(args, 2, args.length);
                    }

                    if (length == manager.PUNISHMENT_EXPIRE_NEVER
                            && !cs.hasPermission("punishments.ban.permanent")) {
                        cs.sendMessage(ChatColor.RED + "You can't ban players for that long");
                        return true;
                    } else if (givenTime > TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS)
                            && !cs.hasPermission("punishments.ban.bypass.templimit")) {
                        cs.sendMessage(ChatColor.RED + "You can't ban players for that long");
                        return true;
                    }

                    PunishmentManager.Punishment punishment = manager.addPunishment(
                            PunishmentManager.PunishmentType.BAN,
                            player.getUniqueId(),
                            admin,
                            cs.getName(),
                            System.currentTimeMillis(),
                            length,
                            manager.getServer(),
                            reason
                    );

                    if (player.isOnline()) {
                        ((Player) player).kickPlayer(punishment.getMessage());
                    }

                    if (length == manager.PUNISHMENT_EXPIRE_NEVER) {
                        Bukkit.broadcastMessage(ChatColor.RED + "Player " + player.getName() + " was banned by " + cs.getName());
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "Player " + player.getName() + " was temporarily banned by " + cs.getName());
                    }
                } else {
                    cs.sendMessage(ChatColor.RED + "Usage: /ban <player> <length> [reason ...]");
                }
            } else {
                cs.sendMessage(ChatColor.RED + "Insufficient permissions.");
            }
        } else if (label.equalsIgnoreCase("mute")) {
            if (cs.hasPermission("punishments.mute")) {
                if (args.length >= 2) {
                    String name = args[0];
                    OfflinePlayer player = Bukkit.getOfflinePlayer(name);

                    if (player == null) {
                        cs.sendMessage(ChatColor.RED + "Cannot find player: " + name);
                        return true;
                    }

                    String reason = null;
                    long length = manager.PUNISHMENT_EXPIRE_NEVER;
                    long givenTime = Util.lengthToMiliseconds(args[1]);
                    if (givenTime != 0) {
                        length = System.currentTimeMillis() + givenTime;
                    }
                    if (args.length >= 3) {
                        reason = Util.argsToString(args, 2, args.length);
                    }


                    if (length == manager.PUNISHMENT_EXPIRE_NEVER
                            && !cs.hasPermission("punishments.ban.permanent")) {
                        cs.sendMessage(ChatColor.RED + "You can't mute players for that long");
                        return true;
                    } else if (givenTime > TimeUnit.MILLISECONDS.convert(3, TimeUnit.HOURS)
                            && !cs.hasPermission("punishments.ban.bypass.templimit")) {
                        cs.sendMessage(ChatColor.RED + "You can't mute players for that long");
                        return true;
                    }


                    PunishmentManager.Punishment punishment = manager.addPunishment(
                            PunishmentManager.PunishmentType.MUTE,
                            player.getUniqueId(),
                            admin,
                            cs.getName(),
                            System.currentTimeMillis(),
                            length,
                            manager.getServer(),
                            reason
                    );

                    if (player.isOnline()) {
                        ((Player) player).sendMessage(punishment.getMessage());
                    }
                    if (length == manager.PUNISHMENT_EXPIRE_NEVER) {
                        Bukkit.broadcastMessage(ChatColor.RED + "Player " + player.getName() + " was muted by " + cs.getName());
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "Player " + player.getName() + " was temporarily muted by " + cs.getName());
                    }
                } else {
                    cs.sendMessage(ChatColor.RED + "Usage: /mute <player> <length> [reason ...]");
                }
            } else {
                cs.sendMessage(ChatColor.RED + "Insufficient permissions.");
            }
        } else if (label.equalsIgnoreCase("unban")) {
            if (cs.hasPermission("punishments.unban")) {
                if (args.length == 1) {
                    String name = args[0];
                    OfflinePlayer player = Bukkit.getOfflinePlayer(name);

                    if (player == null) {
                        cs.sendMessage(ChatColor.RED + "Cannot find player: " + name);
                        return true;
                    }

                    PunishmentManager.Punishment punishment;
                    if ((punishment = manager.hasActivePunishment(player.getUniqueId(), PunishmentManager.PunishmentType.BAN)) != null) {
                        punishment.expire();
                        Bukkit.broadcastMessage(ChatColor.RED + "Player " + player.getName() + " was unbanned by " + cs.getName());
                    } else {
                        cs.sendMessage("Player: " + player.getName() + " not banned");
                    }
                } else {
                    cs.sendMessage(ChatColor.RED + "Usage: /unban <player>");
                }
            } else {
                cs.sendMessage(ChatColor.RED + "Insufficient permissions.");
            }
        } else if (label.equalsIgnoreCase("unmute")) {
            if (cs.hasPermission("punishments.unmute")) {
                if (args.length == 1) {
                    String name = args[0];
                    OfflinePlayer player = Bukkit.getOfflinePlayer(name);

                    if (player == null) {
                        cs.sendMessage(ChatColor.RED + "Cannot find player: " + name);
                        return true;
                    }

                    PunishmentManager.Punishment punishment;
                    if ((punishment = manager.hasActivePunishment(player.getUniqueId(), PunishmentManager.PunishmentType.MUTE)) != null) {
                        punishment.expire();
                        Bukkit.broadcastMessage(ChatColor.RED + "Player " + player.getName() + " was unmuted by " + cs.getName());
                    } else {
                        cs.sendMessage(ChatColor.RED + "Player: " + player.getName() + " is not muted");
                    }
                } else {
                    cs.sendMessage(ChatColor.RED + "Usage: /unmute <player>");
                }
            } else {
                cs.sendMessage(ChatColor.RED + "Insufficient permissions.");
            }
        } else if (label.equalsIgnoreCase("history")) {
            if (cs.hasPermission("punishments.history")) {
                if (args.length == 1) {
                    String name = args[0];
                    OfflinePlayer player = Bukkit.getOfflinePlayer(name);

                    if (player == null) {
                        cs.sendMessage(ChatColor.RED + "Cannot find player: " + name);
                        return true;
                    }

                    cs.sendMessage(ChatColor.YELLOW + "History for: " + player.getName());

                    for (PunishmentManager.Punishment punishment : manager.getAllPunishmentsFor(player.getUniqueId())) {
                        cs.sendMessage(
                                ChatColor.GOLD +
                                        "(" + punishment.getId() + ") " +
                                        ChatColor.GRAY +
                                        Util.formatTimestamp(punishment.getCreated()) +
                                        ": " +
                                        ChatColor.WHITE +
                                        punishment.getMessage()
                        );
                    }
                } else {
                    cs.sendMessage(ChatColor.RED + "Usage: /history <player>");
                }
            } else {
                cs.sendMessage(ChatColor.RED + "Insufficient permissions.");
            }
        }
        return true;
    }
}
