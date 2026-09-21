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

package com.andrei1058.bedwars.qstudio.beds;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import com.andrei1058.bedwars.qstudio.modern.FX;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.andrei1058.bedwars.BedWars.nms;

/**
 * Bed defense enhancements: proximity alarm, the early game protection
 * phase and configurable bed break effects.
 */
public class BedDefenseListener implements Listener {

    /** world -> protection end. */
    private static final Map<String, Long> protectionUntil = new ConcurrentHashMap<>();
    /** team member -> last alarm. */
    private static final Map<UUID, Long> lastAlarm = new ConcurrentHashMap<>();

    @EventHandler
    public void onGameStart(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        IArena arena = e.getArena();
        if (!QStudioConfig.getBoolean(arena.getGroup(), "beds.protection-phase.enabled")) return;

        int duration = QStudioConfig.getInt(arena.getGroup(), "beds.protection-phase.duration-seconds");
        if (duration <= 0) return;
        protectionUntil.put(arena.getWorldName(), System.currentTimeMillis() + duration * 1000L);
    }

    /**
     * Protection countdown display, runs once per second.
     */
    public static void tickProtection() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : protectionUntil.entrySet()) {
            IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByIdentifier(entry.getKey());
            if (arena == null || arena.getStatus() != GameState.playing) {
                protectionUntil.remove(entry.getKey());
                continue;
            }
            long remaining = entry.getValue() - now;
            if (remaining <= 0) {
                protectionUntil.remove(entry.getKey());
                for (Player player : arena.getPlayers()) {
                    nms.playAction(player, org.bukkit.ChatColor.translateAlternateColorCodes('&',
                            com.andrei1058.bedwars.api.language.Language.getMsg(player, "qstudio.bed-protection-over")));
                }
                continue;
            }
            for (Player player : arena.getPlayers()) {
                nms.playAction(player, org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        com.andrei1058.bedwars.api.language.Language.getMsg(player, "qstudio.bed-protection-timer")
                                .replace("{seconds}", String.valueOf((int) Math.ceil(remaining / 1000.0)))));
            }
        }
    }

    /**
     * @return true while the bed protection phase of the arena is active.
     */
    public static boolean isProtectionActive(IArena arena) {
        Long until = protectionUntil.get(arena.getWorldName());
        return until != null && until > System.currentTimeMillis();
    }

    /**
     * Bed proximity alarm, runs once per second.
     */
    public static void tickAlarm() {
        for (IArena arena : com.andrei1058.bedwars.arena.Arena.getArenas()) {
            if (arena.getStatus() != GameState.playing) continue;
            if (!QStudioConfig.getBoolean(arena.getGroup(), "beds.alarm.enabled")) continue;
            double radius = QStudioConfig.getDouble(arena.getGroup(), "beds.alarm.radius");
            int interval = QStudioConfig.getInt(arena.getGroup(), "beds.alarm.warn-interval-seconds");

            for (ITeam team : arena.getTeams()) {
                if (team.isBedDestroyed() || team.getBed() == null) continue;
                for (Player enemy : arena.getPlayers()) {
                    if (team.isMember(enemy)) continue;
                    if (arena.isSpectator(enemy)) continue;
                    if (enemy.getLocation().distance(team.getBed()) > radius) continue;

                    String template = QStudioConfig.getString(arena.getGroup(), "beds.alarm.action-bar");
                    for (Player member : team.getMembers()) {
                        long last = lastAlarm.getOrDefault(member.getUniqueId(), 0L);
                        if (System.currentTimeMillis() - last < interval * 1000L) continue;
                        lastAlarm.put(member.getUniqueId(), System.currentTimeMillis());
                        nms.playAction(member, org.bukkit.ChatColor.translateAlternateColorCodes('&',
                                template.replace("{player}", enemy.getName())));
                        FX.playSound(member, FX.versioned("NOTE_PLING", "BLOCK_NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBedBreak(PlayerBedBreakEvent e) {
        IArena arena = e.getArena();
        String group = arena.getGroup();

        if (QStudioConfig.getBoolean(group, "beds.break-effects.particles")) {
            if (e.getVictimTeam().getBed() != null) {
                FX.playEffect(e.getVictimTeam().getBed(), FX.versioned("LAVA_POP", "LAVA_POP", "LAVA_POP"));
                FX.playEffect(e.getVictimTeam().getBed().add(0, 1, 0), FX.versioned("SMOKE", "SMOKE", "SMOKE"));
            }
        }

        if (QStudioConfig.getBoolean(group, "beds.break-effects.global-notification")) {
            for (Player player : arena.getPlayers()) {
                player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        com.andrei1058.bedwars.api.language.Language.getMsg(player, "qstudio.bed-broken-global")
                                .replace("{player}", e.getPlayer().getName())
                                .replace("{team}", e.getVictimTeam().getDisplayName(
                                        com.andrei1058.bedwars.api.language.Language.getPlayerLanguage(player)))));
            }
        }
    }

    public static void cleanupArena(String world) {
        protectionUntil.remove(world);
        HashMap<UUID, Long> keep = new HashMap<>();
        lastAlarm.forEach((uuid, time) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && !player.getWorld().getName().equals(world)) {
                keep.put(uuid, time);
            }
        });
        lastAlarm.clear();
        lastAlarm.putAll(keep);
    }
}
