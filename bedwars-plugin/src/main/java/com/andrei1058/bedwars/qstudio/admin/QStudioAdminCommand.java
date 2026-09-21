/*
 * BedWars1058-qStudio
 * Based on BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.qstudio.admin;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.commands.bedwars.MainCommand;
import com.andrei1058.bedwars.qstudio.config.DebugLog;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /bw qstudio diagnostics|validate|debug|reload
 * <p>
 * Administrative inspection of arenas and qStudio configuration.
 */
public class QStudioAdminCommand extends SubCommand {

    public QStudioAdminCommand(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(14);
        setPermission("bw.qstudio.admin");
        showInList(false);
        setDisplayInfo(MainCommand.createTC("§6 ▪ §7/" + MainCommand.getInstance().getName() + " " + getSubCommandName(),
                "/" + getParent().getName() + " " + getSubCommandName(), "§fqStudio administration."));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (!(s instanceof ConsoleCommandSender) && !(s instanceof Player)) return false;
        if (args.length == 0) {
            sendHelp(s);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "diagnostics":
                if (args.length < 2) {
                    s.sendMessage(ChatColor.RED + "Usage: /bw qstudio diagnostics <arena>");
                    return true;
                }
                sendDiagnostics(s, args[1]);
                return true;
            case "validate": {
                List<String> problems = ConfigValidator.validate();
                if (problems.isEmpty()) {
                    s.sendMessage(ChatColor.GREEN + "qStudio configuration looks good.");
                } else {
                    s.sendMessage(ChatColor.YELLOW + "qStudio validation found " + problems.size() + " problem(s):");
                    for (String problem : problems) {
                        s.sendMessage(ChatColor.RED + " - " + problem);
                    }
                }
                return true;
            }
            case "debug":
                DebugLog.setEnabled(!DebugLog.isEnabled());
                s.sendMessage(ChatColor.YELLOW + "qStudio debug: " + (DebugLog.isEnabled() ? "ON" : "OFF"));
                return true;
            case "reload":
                QStudioConfig.reload();
                s.sendMessage(ChatColor.GREEN + "qStudio configuration reloaded.");
                return true;
            default:
                sendHelp(s);
                return true;
        }
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(ChatColor.AQUA + "qStudio admin:");
        s.sendMessage(ChatColor.GRAY + "/bw qstudio diagnostics <arena>");
        s.sendMessage(ChatColor.GRAY + "/bw qstudio validate");
        s.sendMessage(ChatColor.GRAY + "/bw qstudio debug");
        s.sendMessage(ChatColor.GRAY + "/bw qstudio reload");
    }

    private void sendDiagnostics(CommandSender s, String arenaName) {
        IArena arena = Arena.getArenaByName(arenaName);
        if (arena == null) {
            s.sendMessage(ChatColor.RED + "Arena not found: " + arenaName);
            return;
        }
        s.sendMessage(ChatColor.AQUA + "Arena " + arena.getArenaName() + ":");
        s.sendMessage(ChatColor.GRAY + "Status: " + arena.getStatus() + ", group: " + arena.getGroup());
        s.sendMessage(ChatColor.GRAY + "Players: " + arena.getPlayers().size() + ", spectators: " + arena.getSpectators().size());
        for (ITeam team : arena.getTeams()) {
            s.sendMessage(ChatColor.GRAY + "Team " + team.getName() + ": members=" + team.getMembers().size()
                    + " bed=" + (!team.isBedDestroyed()) + " traps=" + team.getActiveTraps().size()
                    + " forge=" + com.andrei1058.bedwars.qstudio.upgrades.GeneratorBoostManager.getForgeLevel(team));
        }
    }

    @Override
    public List<String> getTabComplete() {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("diagnostics");
        suggestions.add("validate");
        suggestions.add("debug");
        suggestions.add("reload");
        return suggestions;
    }
}
