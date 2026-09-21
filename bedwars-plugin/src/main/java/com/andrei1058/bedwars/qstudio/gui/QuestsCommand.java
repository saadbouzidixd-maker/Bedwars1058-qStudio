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

package com.andrei1058.bedwars.qstudio.gui;

import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.commands.bedwars.MainCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * /bw quests - opens the quest overview.
 */
public class QuestsCommand extends SubCommand {

    public QuestsCommand(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(15);
        showInList(false);
        setDisplayInfo(MainCommand.createTC("§6 ▪ §7/" + MainCommand.getInstance().getName() + " " + getSubCommandName(),
                "/" + getParent().getName() + " " + getSubCommandName(), "§fOpens the qStudio quests GUI."));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (!(s instanceof ConsoleCommandSender) && s instanceof Player) {
            QuestGUI.open((Player) s);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getTabComplete() {
        return Collections.emptyList();
    }
}
