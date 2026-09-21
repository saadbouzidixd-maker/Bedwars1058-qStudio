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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of qStudio team auras (regen, defense, reveal) and trap
 * capacity bonuses. Active auras are applied by the qStudio heartbeat
 * task while players stand on the island.
 */
public final class AuraRegistry {

    /** world;team -> amplifier. */
    private static final Map<String, Integer> regenAuras = new ConcurrentHashMap<>();
    private static final Map<String, Integer> defenseAuras = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> revealAuras = new ConcurrentHashMap<>();
    private static final Map<String, Integer> trapCapacity = new ConcurrentHashMap<>();

    private AuraRegistry() {
    }

    private static String key(ITeam team) {
        return team.getArena().getWorldName() + ";" + team.getName();
    }

    public static void enableRegenAura(ITeam team, int amplifier) {
        regenAuras.put(key(team), amplifier);
    }

    public static void enableDefenseAura(ITeam team, int amplifier) {
        defenseAuras.put(key(team), amplifier);
    }

    public static void enableRevealAura(ITeam team) {
        revealAuras.put(key(team), Boolean.TRUE);
    }

    public static boolean hasRevealAura(ITeam team) {
        return revealAuras.containsKey(key(team));
    }

    public static void addCapacity(ITeam team, int amount) {
        trapCapacity.merge(key(team), amount, Integer::sum);
    }

    public static int getTrapCapacityBonus(ITeam team) {
        Integer bonus = trapCapacity.get(key(team));
        return bonus == null ? 0 : bonus;
    }

    /**
     * Apply auras to players on their island, runs once per second.
     */
    public static void tick() {
        for (IArena arena : com.andrei1058.bedwars.arena.Arena.getArenas()) {
            if (arena.getStatus() != com.andrei1058.bedwars.api.arena.GameState.playing) continue;
            for (ITeam team : arena.getTeams()) {
                String k = key(team);
                Integer regen = regenAuras.get(k);
                Integer defense = defenseAuras.get(k);
                boolean reveal = revealAuras.containsKey(k);
                if (regen == null && defense == null && !reveal) continue;

                for (org.bukkit.entity.Player member : team.getMembers()) {
                    if (regen != null && member.getLocation().distance(team.getBed()) <= arena.getIslandRadius()) {
                        member.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, regen, true));
                    }
                }
                for (org.bukkit.entity.Player enemy : arena.getPlayers()) {
                    if (team.isMember(enemy)) continue;
                    if (enemy.getLocation().distance(team.getBed()) > arena.getIslandRadius()) continue;
                    if (defense != null) {
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, defense, true));
                    }
                    if (reveal && enemy.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        enemy.removePotionEffect(PotionEffectType.INVISIBILITY);
                    }
                }
            }
        }
    }

    public static void cleanupArena(String world) {
        regenAuras.keySet().removeIf(k -> k.startsWith(world + ";"));
        defenseAuras.keySet().removeIf(k -> k.startsWith(world + ";"));
        revealAuras.keySet().removeIf(k -> k.startsWith(world + ";"));
        trapCapacity.keySet().removeIf(k -> k.startsWith(world + ";"));
    }
}
