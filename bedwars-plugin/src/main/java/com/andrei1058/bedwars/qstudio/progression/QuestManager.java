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
import com.andrei1058.bedwars.api.qstudio.events.QSQuestCompleteEvent;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quest system: daily, weekly and one time quests loaded from
 * progression.quest-list. Progress is persisted per player through the
 * remote database and reset when the frequency period rolls over.
 */
public class QuestManager {

    /** quest id -> definition. */
    private static final Map<String, QuestDefinition> quests = new LinkedHashMap<>();
    /** player -> quest id -> progress. */
    private static final Map<UUID, Map<String, Integer>> progress = new ConcurrentHashMap<>();
    /** player -> quest id -> completion period stamp (daily/weekly) or -1 for once. */
    private static final Map<UUID, Map<String, Long>> completions = new ConcurrentHashMap<>();

    private QuestManager() {
    }

    public static void init() {
        quests.clear();
        var yml = QStudioConfig.getConfig().getYml();
        if (!yml.isSet("progression.quest-list")) return;
        for (String id : yml.getConfigurationSection("progression.quest-list").getKeys(false)) {
            String base = "progression.quest-list." + id;
            String type = yml.getString(base + ".type", "");
            if (type.isEmpty()) {
                BedWars.plugin.getLogger().warning("qStudio: quest " + id + " has no type, skipped.");
                continue;
            }
            String frequency = yml.getString(base + ".frequency", QuestDefinition.ONCE);
            int amount = yml.getInt(base + ".amount", 1);
            int rewardXp = yml.getInt(base + ".reward-xp", 0);
            List<String> commands = yml.getStringList(base + ".reward-commands");
            quests.put(id, new QuestDefinition(id, type, frequency, amount, rewardXp, commands));
        }
        BedWars.debug("qStudio: loaded " + quests.size() + " quests");
    }

    public static Map<String, QuestDefinition> getQuests() {
        return quests;
    }

    public static QuestDefinition getQuest(String id) {
        return quests.get(id);
    }

    public static int getProgress(UUID player, String questId) {
        Map<String, Integer> map = progress.get(player);
        Integer value = map == null ? null : map.get(questId);
        return value == null ? 0 : value;
    }

    public static boolean isCompleted(UUID player, String questId) {
        Map<String, Long> map = completions.get(player);
        return map != null && map.containsKey(questId);
    }

    public static List<String> getCompletedQuestIds(UUID player) {
        Map<String, Long> map = completions.get(player);
        return map == null ? new ArrayList<>() : new ArrayList<>(map.keySet());
    }

    /**
     * Increment a quest objective for a player.
     */
    public static void addProgress(Player player, String type, int amount) {
        if (amount <= 0) return;
        for (QuestDefinition quest : quests.values()) {
            if (!quest.getType().equals(type)) continue;
            if (isExpired(player.getUniqueId(), quest)) continue;
            if (isCompleted(player.getUniqueId(), quest.getId())) continue;

            Map<String, Integer> playerProgress =
                    progress.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
            int value = playerProgress.merge(quest.getId(), amount, Integer::sum);

            if (value >= quest.getAmount()) {
                complete(player, quest, playerProgress);
            } else if (value > 0) {
                BedWars.nms.playAction(player, org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        com.andrei1058.bedwars.api.language.Language.getMsg(player, "qstudio.quest-progress")
                                .replace("{quest}", quest.getId())
                                .replace("{progress}", String.valueOf(value))
                                .replace("{amount}", String.valueOf(quest.getAmount()))));
            }
        }
    }

    private static void complete(Player player, QuestDefinition quest, Map<String, Integer> playerProgress) {
        playerProgress.put(quest.getId(), quest.getAmount());
        completions.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(quest.getId(), currentPeriod(quest.getFrequency()));

        Bukkit.getPluginManager().callEvent(new QSQuestCompleteEvent(player, quest.getId()));
        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                com.andrei1058.bedwars.api.language.Language.getMsg(player, "qstudio.quest-completed")
                        .replace("{quest}", quest.getId())));
        RewardService.giveAll(player, quest.getRewardXp(), quest.getRewardCommands());
        persistAsync(player.getUniqueId());
    }

    private static boolean isExpired(UUID player, QuestDefinition quest) {
        Map<String, Long> map = completions.get(player);
        if (map == null) return false;
        Long period = map.get(quest.getId());
        return period != null && period != currentPeriod(quest.getFrequency());
    }

    /**
     * Period stamp of a frequency: epoch day for daily, epoch week for
     * weekly, -1 (never resets) for one time quests.
     */
    private static long currentPeriod(String frequency) {
        long day = System.currentTimeMillis() / 86_400_000L;
        if (QuestDefinition.WEEKLY.equals(frequency)) return day / 7;
        if (QuestDefinition.DAILY.equals(frequency)) return day;
        return -1L;
    }

    private static void persistAsync(UUID player) {
        // capture current state synchronously, then write on the async thread
        String progressRaw = serialize(progress.get(player));
        String completionsRaw = serializeLong(completions.get(player));
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () ->
                BedWars.getRemoteDatabase().saveQuestData(player, progressRaw, completionsRaw));
    }

    private static String serialize(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (builder.length() > 0) builder.append(";");
            builder.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return builder.toString();
    }

    private static String serializeLong(Map<String, Long> map) {
        if (map == null || map.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            if (builder.length() > 0) builder.append(";");
            builder.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return builder.toString();
    }

    private static Map<String, Integer> deserialize(String raw) {
        Map<String, Integer> map = new HashMap<>();
        if (raw == null || raw.isEmpty()) return map;
        for (String entry : raw.split(";")) {
            String[] parts = entry.split(":");
            if (parts.length != 2) continue;
            try {
                map.put(parts[0], Integer.parseInt(parts[1]));
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }

    private static Map<String, Long> deserializeLong(String raw) {
        Map<String, Long> map = new HashMap<>();
        if (raw == null || raw.isEmpty()) return map;
        for (String entry : raw.split(";")) {
            String[] parts = entry.split(":");
            if (parts.length != 2) continue;
            try {
                map.put(parts[0], Long.parseLong(parts[1]));
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }

    /**
     * Load persisted quest state when a player joins.
     */
    public static void loadPlayer(UUID player) {
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () -> {
            String progressRaw = BedWars.getRemoteDatabase().getQuestProgress(player);
            String completionsRaw = BedWars.getRemoteDatabase().getQuestCompletions(player);
            Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
                progress.put(player, new ConcurrentHashMap<>(deserialize(progressRaw)));
                completions.put(player, new ConcurrentHashMap<>(deserializeLong(completionsRaw)));
            });
        });
    }

    public static void unloadPlayer(UUID player) {
        persistAsync(player);
        progress.remove(player);
        completions.remove(player);
    }
}
