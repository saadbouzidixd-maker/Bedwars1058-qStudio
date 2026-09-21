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

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.api.events.player.PlayerGeneratorCollectEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Maps game events to quest and achievement progress.
 */
public class QuestListener implements Listener {

    private boolean enabled() {
        return QStudioConfig.getBoolean(null, "progression.quests.enabled")
                || QStudioConfig.getBoolean(null, "progression.achievements.enabled");
    }

    @EventHandler
    public void onKill(PlayerKillEvent e) {
        Player player = e.getKiller();
        if (player == null || !enabled()) return;
        if (e.getCause().isFinalKill()) {
            QuestManager.addProgress(player, QuestDefinition.FINAL_KILLS, 1);
            evaluateStats(player, "kills", null);
        } else {
            QuestManager.addProgress(player, QuestDefinition.KILLS, 1);
            evaluateStats(player, "kills", null);
        }
    }

    @EventHandler
    public void onBedBreak(PlayerBedBreakEvent e) {
        Player player = e.getPlayer();
        if (player == null || !enabled()) return;
        QuestManager.addProgress(player, QuestDefinition.BEDS_BROKEN, 1);
        evaluateStats(player, "beds_broken", null);
        int xp = QStudioConfig.getInt(e.getArena().getGroup(), "progression.xp-per-bed-broken");
        RewardService.giveXp(player, xp);
    }

    @EventHandler
    public void onCollect(PlayerGeneratorCollectEvent e) {
        Player player = e.getPlayer();
        if (player == null || !enabled()) return;
        QuestManager.addProgress(player, QuestDefinition.RESOURCES_COLLECTED, 1);
    }

    @EventHandler
    public void onGameEnd(GameEndEvent e) {
        if (!enabled()) return;
        for (UUID winner : e.getWinners()) {
            Player player = Bukkit.getPlayer(winner);
            if (player == null) continue;
            QuestManager.addProgress(player, QuestDefinition.WINS, 1);
            evaluateStats(player, "wins", null);
            int xp = QStudioConfig.getInt(e.getArena().getGroup(), "progression.xp-per-win");
            RewardService.giveXp(player, xp);
        }
        for (UUID loser : e.getLosers()) {
            Player player = Bukkit.getPlayer(loser);
            if (player == null) continue;
            evaluateStats(player, "games_played", null);
        }
    }

    /**
     * Evaluate achievements from the persistent stats of a player.
     * The stats cache is updated by the game end handlers before this
     * runs, so achievements unlock with the persisted totals.
     */
    private void evaluateStats(Player player, String type, Integer overrideValue) {
        var stats = BedWars.getStatsManager().get(player.getUniqueId());
        if (stats == null) return;
        int value;
        switch (type) {
            case "kills":
                value = stats.getTotalKills();
                break;
            case "beds_broken":
                value = stats.getBedsDestroyed();
                break;
            case "wins":
                value = stats.getWins();
                break;
            case "games_played":
                value = stats.getGamesPlayed();
                break;
            default:
                return;
        }
        AchievementManager.evaluate(player, type, value);
    }
}
