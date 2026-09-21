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

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks marked enemies and restores their display name when the mark
 * expires.
 */
public final class MarkingTracker {

    private static final class Mark {
        private final long expiry;
        private final String originalName;

        Mark(long expiry, String originalName) {
            this.expiry = expiry;
            this.originalName = originalName;
        }
    }

    private static final Map<UUID, Mark> marked = new ConcurrentHashMap<>();

    private MarkingTracker() {
    }

    public static void mark(UUID player, long expiry, String originalName) {
        marked.put(player, new Mark(expiry, originalName));
    }

    /**
     * Restore expired names, runs once per second.
     */
    public static void tick() {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Mark> entry : marked.entrySet()) {
            if (entry.getValue().expiry > now) continue;
            Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                player.setDisplayName(entry.getValue().originalName);
            }
            marked.remove(entry.getKey());
        }
    }

    public static void cleanup() {
        for (Map.Entry<UUID, Mark> entry : marked.entrySet()) {
            Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                player.setDisplayName(entry.getValue().originalName);
            }
        }
        marked.clear();
    }
}
