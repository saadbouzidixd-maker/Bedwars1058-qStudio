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

package com.andrei1058.bedwars.api.qstudio;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Public access to qStudio gameplay systems.
 */
public interface QStudioApi {

    /**
     * Build the display item of a qStudio modern item.
     *
     * @param type modern item identifier, see {@link ModernItems}.
     * @return the display item or null when the type is unknown/disabled.
     */
    ItemStack createModernItem(String type);

    /**
     * Check if a stack holds a qStudio modern item identifier.
     */
    String getModernItemId(ItemStack stack);

    /**
     * Remaining seconds before the player can use the given modern item again.
     */
    int getCooldownSeconds(UUID player, String type);

    /**
     * Progress of a quest objective for a player (0 if unknown).
     */
    int getQuestProgress(UUID player, String questId);

    /**
     * Ids of the quests the player already completed.
     */
    List<String> getCompletedQuests(UUID player);

    /**
     * Ids of the achievements unlocked by the player.
     */
    List<String> getUnlockedAchievements(UUID player);

    /**
     * Current level of a team upgrade bought through the qStudio forge system.
     */
    int getForgeLevel(ITeam team);

    /**
     * Live generator status for diagnostics and sidebars.
     *
     * @return map with arena name as key and a list of generator descriptions.
     */
    Map<String, List<String>> getGeneratorStatus(IArena arena);
}
