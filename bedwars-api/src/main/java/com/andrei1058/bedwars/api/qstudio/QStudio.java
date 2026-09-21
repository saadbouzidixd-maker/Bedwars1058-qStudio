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

/**
 * Entry point of the qStudio public API.
 * <p>
 * The implementation is registered while the plugin enables and can be
 * resolved through {@link #get()} or through the Bukkit services manager.
 */
public final class QStudio {

    private static QStudioApi instance;

    private QStudio() {
    }

    /**
     * Register the API implementation. Internal use.
     */
    public static void register(QStudioApi api) {
        if (api == null) return;
        QStudio.instance = api;
    }

    /**
     * @return the API instance or null if the plugin did not register it yet.
     */
    public static QStudioApi get() {
        return instance;
    }
}
