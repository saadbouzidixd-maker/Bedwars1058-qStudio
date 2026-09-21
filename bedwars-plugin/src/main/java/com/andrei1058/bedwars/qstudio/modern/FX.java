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

package com.andrei1058.bedwars.qstudio.modern;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.qstudio.config.DebugLog;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Version safe particle and sound helpers used by the modern item layer.
 * <p>
 * Effects are referenced by legacy effect names so the same configuration
 * works on 1.8 and on modern releases.
 */
public final class FX {

    private FX() {
    }

    /**
     * Play a legacy particle effect at a location. Unknown names are ignored.
     */
    public static void playEffect(Location location, String effectName) {
        if (effectName == null || effectName.isEmpty() || effectName.equalsIgnoreCase("none")) return;
        try {
            Effect effect = Effect.valueOf(effectName.toUpperCase());
            location.getWorld().playEffect(location, effect, 0);
        } catch (IllegalArgumentException ex) {
            DebugLog.log("Unknown effect: " + effectName);
        }
    }

    /**
     * Play a sound from a config value with per-version default fallback.
     *
     * @param soundPath config path under modern-items.<id>.sound
     * @param fallback  getForCurrentVersion triple joined by | (old|mid|new)
     */
    public static void playSound(Location location, String soundName) {
        if (soundName == null || soundName.isEmpty() || soundName.equalsIgnoreCase("none")) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            location.getWorld().playSound(location, sound, 1f, 1f);
        } catch (IllegalArgumentException ex) {
            DebugLog.log("Unknown sound: " + soundName);
        }
    }

    /**
     * Play a sound only to one player, ignoring invalid names.
     */
    public static void playSound(Player player, String soundName) {
        if (player == null || soundName == null || soundName.isEmpty() || soundName.equalsIgnoreCase("none")) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (IllegalArgumentException ex) {
            DebugLog.log("Unknown sound: " + soundName);
        }
    }

    /**
     * Safe server version sound name resolution for custom defaults.
     */
    public static String versioned(String oldName, String midName, String newName) {
        return BedWars.getForCurrentVersion(oldName, midName, newName);
    }

    /**
     * Log helper for fx debugging.
     */
    public static void debug(String message) {
        if (DebugLog.isEnabled()) {
            java.util.logging.Logger.getLogger("qStudio").log(Level.FINE, message);
        }
    }
}
