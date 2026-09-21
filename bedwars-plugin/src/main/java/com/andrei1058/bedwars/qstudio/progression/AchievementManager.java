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
import com.andrei1058.bedwars.api.qstudio.events.QSAchievementUnlockEvent;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Achievement system. Definitions come from progression.achievement-list
 * and are lifetime goals evaluated against the persistent player stats.
 */
public class AchievementManager {

    /** achievement id -> definition. */
    private static final Map<String, AchievementDefinition> achievements = new ConcurrentHashMap<>();
    /** player -> unlocked achievement ids. */
    private static final Map<UUID, Set<String>> unlocked = new ConcurrentHashMap<>();

    private AchievementManager() {
    }

    public static void init() {
        achievements.clear();
        var yml = QStudioConfig.getConfig().getYml();
        if (!yml.isSet("progression.achievement-list")) return;
        for (String id : yml.getConfigurationSection("progression.achievement-list").getKeys(false)) {
            String base = "progression.achievement-list." + id;
            String type = yml.getString(base + ".type", "");
            if (type.isEmpty()) {
                BedWars.plugin.getLogger().warning("qStudio: achievement " + id + " has no type, skipped.");
                continue;
            }
            achievements.put(id, new AchievementDefinition(id, type,
                    yml.getInt(base + ".amount", 1),
                    yml.getInt(base + ".reward-xp", 0),
                    yml.getStringList(base + ".reward-commands")));
        }
        BedWars.debug("qStudio: loaded " + achievements.size() + " achievements");
    }

    public static Map<String, AchievementDefinition> getAchievements() {
        return achievements;
    }

    public static List<String> getUnlockedIds(UUID player) {
        Set<String> ids = unlocked.get(player);
        return ids == null ? new ArrayList<>() : new ArrayList<>(ids);
    }

    public static boolean isUnlocked(UUID player, String achievementId) {
        Set<String> ids = unlocked.get(player);
        return ids != null && ids.contains(achievementId);
    }

    /**
     * Evaluate all achievements for a player against a stat value.
     *
     * @param type  objective type (kills, beds_broken, games_played, ...)
     * @param value current lifetime value of that stat
     */
    public static void evaluate(Player player, String type, int value) {
        for (AchievementDefinition achievement : achievements.values()) {
            if (!achievement.getType().equals(type)) continue;
            if (isUnlocked(player.getUniqueId(), achievement.getId())) continue;
            if (value < achievement.getAmount()) continue;

            unlocked.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet())
                    .add(achievement.getId());
            Bukkit.getPluginManager().callEvent(new QSAchievementUnlockEvent(player, achievement.getId()));
            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    com.andrei1058.bedwars.api.language.Language.getMsg(player, "qstudio.achievement-unlocked")
                            .replace("{achievement}", achievement.getId())));
            RewardService.giveAll(player, achievement.getRewardXp(), achievement.getRewardCommands());
            persistAsync(player.getUniqueId());
        }
    }

    private static void persistAsync(UUID player) {
        Set<String> ids = unlocked.get(player);
        if (ids == null) return;
        StringBuilder raw = new StringBuilder();
        for (String id : ids) {
            if (raw.length() > 0) raw.append(";");
            raw.append(id);
        }
        String payload = raw.toString();
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () ->
                BedWars.getRemoteDatabase().saveAchievements(player, payload));
    }

    /**
     * Load unlocked achievements when a player joins.
     */
    public static void loadPlayer(UUID player) {
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () -> {
            String raw = BedWars.getRemoteDatabase().getAchievements(player);
            Set<String> ids = ConcurrentHashMap.newKeySet();
            if (raw != null && !raw.isEmpty()) {
                for (String id : raw.split(";")) {
                    if (!id.isEmpty()) ids.add(id);
                }
            }
            Bukkit.getScheduler().runTask(BedWars.plugin, () -> unlocked.put(player, ids));
        });
    }

    public static void unloadPlayer(UUID player) {
        persistAsync(player);
        unlocked.remove(player);
    }
}
