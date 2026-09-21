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

package com.andrei1058.bedwars.qstudio.config;

/**
 * Scoped debug logger for the qStudio layer.
 * Enabled from qstudio.yml or through /bw qstudio debug.
 */
public final class DebugLog {

    private static boolean enabled = false;

    private DebugLog() {
    }

    public static void setEnabled(boolean enabled) {
        DebugLog.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void log(String message) {
        if (enabled) {
            com.andrei1058.bedwars.BedWars.plugin.getLogger().info("[qStudio-debug] " + message);
        }
    }
}
