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
 * Standard identifiers of the modern item compatibility layer.
 * <p>
 * Each identifier maps to a version specific display item but shares the
 * same behaviour across server versions.
 */
public final class ModernItems {

    public static final String MACE = "mace";
    public static final String WIND_CHARGE = "wind-charge";
    public static final String FIREWORK_BOOST = "firework-boost";
    public static final String DASH = "dash";
    public static final String GRAPPLING_HOOK = "grappling-hook";
    public static final String TEMPORARY_BRIDGE = "temporary-bridge";
    public static final String BRIDGE_BUILDER = "bridge-builder";
    public static final String TELEPORT_BEACON = "teleport-beacon";
    public static final String EMERGENCY_TELEPORT = "emergency-teleport";
    public static final String BASE_SHIELD = "base-shield";
    public static final String EMP_PULSE = "emp-pulse";

    private ModernItems() {
    }
}
