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

package com.andrei1058.bedwars.qstudio.progression;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.events.player.PlayerXpGainEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Dispatches quest and achievement rewards: qStudio xp and console
 * commands with {player} placeholder.
 */
public final class RewardService {

    private RewardService() {
    }

    public static void giveXp(Player player, int xp) {
        if (xp <= 0 || player == null) return;
        BedWars.getLevelSupport().addXp(player, xp, PlayerXpGainEvent.XpSource.OTHER);
    }

    public static void giveCommands(Player player, List<String> commands) {
        if (commands == null || commands.isEmpty() || player == null) return;
        for (String command : commands) {
            String finalCommand = command.replace("{player}", player.getName());
            Bukkit.getScheduler().runTask(BedWars.plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand));
        }
    }

    public static void giveAll(Player player, int xp, List<String> commands) {
        giveXp(player, xp);
        giveCommands(player, commands);
    }
}
