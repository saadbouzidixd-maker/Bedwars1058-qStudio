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

package com.andrei1058.bedwars.qstudio.upgrades;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks forge levels, temporary generator boosts and the pickup range
 * bonus of every team. Generators consult this manager when spawning
 * resources.
 */
public final class GeneratorBoostManager {

    /** world + team name -> forge level. */
    private static final Map<String, Integer> forgeLevels = new ConcurrentHashMap<>();
    /** world + team name -> boost expiry. */
    private static final Map<String, Long> boostExpiry = new ConcurrentHashMap<>();
    /** world -> global boost expiry (boost events). */
    private static final Map<String, Long> arenaBoost = new ConcurrentHashMap<>();
    /** world -> next scheduled boost event. */
    private static final Map<String, Long> nextBoostEvent = new ConcurrentHashMap<>();

    private GeneratorBoostManager() {
    }

    private static String key(ITeam team) {
        return team.getArena().getWorldName() + ";" + team.getName();
    }

    /* forge */

    public static void setForgeLevel(ITeam team, int level) {
        forgeLevels.put(key(team), level);
    }

    public static int getForgeLevel(ITeam team) {
        Integer level = forgeLevels.get(key(team));
        return level == null ? 0 : level;
    }

    public static int getForgeBonusAmount(ITeam team) {
        int level = getForgeLevel(team);
        return level <= 0 ? 0 : level; // one extra resource per spawn per forge level
    }

    public static double getPickupRangeBonus(ITeam team) {
        int level = getForgeLevel(team);
        if (level <= 0) return 0;
        double perLevel = QStudioConfig.getDouble(team.getArena().getGroup(),
                "generators.pickup-range.forge-bonus-per-level");
        return level * perLevel;
    }

    /* boosts */

    public static void setBoost(ITeam team, int seconds) {
        boostExpiry.put(key(team), System.currentTimeMillis() + seconds * 1000L);
    }

    public static double getSpeedMultiplier(ITeam team) {
        String world = team.getArena().getWorldName();
        double multiplier = 1.0;

        Long teamBoost = boostExpiry.get(key(team));
        if (teamBoost != null && teamBoost > System.currentTimeMillis()) {
            multiplier *= 2.0;
        }
        Long globalBoost = arenaBoost.get(world);
        if (globalBoost != null && globalBoost > System.currentTimeMillis()) {
            multiplier *= 2.0;
        }
        return multiplier;
    }

    /**
     * Start a temporary global boost for an arena (boost event).
     */
    public static void startArenaBoost(IArena arena, int seconds) {
        arenaBoost.put(arena.getWorldName(), System.currentTimeMillis() + seconds * 1000L);
    }

    public static boolean isArenaBoostActive(IArena arena) {
        Long expiry = arenaBoost.get(arena.getWorldName());
        return expiry != null && expiry > System.currentTimeMillis();
    }

    /**
     * Handle the scheduled boost events, runs once per second.
     */
    public static void tickBoostEvents() {
        for (IArena arena : com.andrei1058.bedwars.arena.Arena.getArenas()) {
            if (arena.getStatus() != com.andrei1058.bedwars.api.arena.GameState.playing) continue;
            if (!QStudioConfig.getBoolean(arena.getGroup(), "generators.boost-events.enabled")) continue;

            Long next = nextBoostEvent.get(arena.getWorldName());
            long now = System.currentTimeMillis();
            if (next == null) {
                int interval = QStudioConfig.getInt(arena.getGroup(), "generators.boost-events.interval-seconds");
                nextBoostEvent.put(arena.getWorldName(), now + interval * 1000L);
                continue;
            }
            if (now < next || isArenaBoostActive(arena)) continue;

            int duration = QStudioConfig.getInt(arena.getGroup(), "generators.boost-events.duration-seconds");
            startArenaBoost(arena, duration);
            int interval = QStudioConfig.getInt(arena.getGroup(), "generators.boost-events.interval-seconds");
            nextBoostEvent.put(arena.getWorldName(), now + interval * 1000L);

            if (QStudioConfig.getBoolean(arena.getGroup(), "generators.boost-events.announce")) {
                for (Player player : arena.getPlayers()) {
                    player.sendMessage(com.andrei1058.bedwars.api.language.Language.getMsg(player,
                            "qstudio.generator-boost-start").replace("{seconds}", String.valueOf(duration)));
                }
            }
        }
    }

    /**
     * Expire boosts and apply the pickup range magnet, runs once per second.
     */
    public static void tick() {
        long now = System.currentTimeMillis();
        boostExpiry.values().removeIf(expiry -> expiry <= now);
        arenaBoost.values().removeIf(expiry -> expiry <= now);

        for (IArena arena : com.andrei1058.bedwars.arena.Arena.getArenas()) {
            if (arena.getStatus() != com.andrei1058.bedwars.api.arena.GameState.playing) continue;
            for (ITeam team : arena.getTeams()) {
                double bonus = getPickupRangeBonus(team);
                if (bonus <= 0) continue;
                for (Player member : team.getMembers()) {
                    for (org.bukkit.entity.Entity entity : member.getNearbyEntities(bonus, bonus, bonus)) {
                        if (!(entity instanceof Item)) continue;
                        Item item = (Item) entity;
                        var velocity = member.getLocation().toVector()
                                .subtract(item.getLocation().toVector());
                        if (velocity.lengthSquared() < 0.04) continue;
                        item.setVelocity(velocity.normalize().multiply(0.35));
                    }
                }
            }
        }
    }

    public static void cleanupArena(String world) {
        forgeLevels.keySet().removeIf(k -> k.startsWith(world + ";"));
        boostExpiry.keySet().removeIf(k -> k.startsWith(world + ";"));
        arenaBoost.remove(world);
        nextBoostEvent.remove(world);
    }
}
