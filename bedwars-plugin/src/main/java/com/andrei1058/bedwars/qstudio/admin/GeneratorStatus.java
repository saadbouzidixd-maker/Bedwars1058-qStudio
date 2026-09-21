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

package com.andrei1058.bedwars.qstudio.admin;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.generator.GeneratorType;
import com.andrei1058.bedwars.api.arena.generator.IGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime overview of every generator of an arena: type, spawn delay,
 * current ore and per team ownership. Used by the diagnostics command
 * and exposed through the qStudio API.
 */
public final class GeneratorStatus {

    private GeneratorStatus() {
    }

    public static Map<String, List<String>> report(IArena arena) {
        Map<String, List<String>> report = new HashMap<>();
        for (IGenerator generator : arena.getOreGenerators()) {
            String type = generator.getType() == null ? "unknown" : generator.getType().name().toLowerCase();
            String owner = generator.getBwt() == null ? "map" : generator.getBwt().getName();

            String key = type + ":" + owner;
            List<String> lines = report.get(key);
            if (lines == null) {
                lines = new ArrayList<>();
                report.put(key, lines);
            }
            lines.add(describe(generator));
        }
        return report;
    }

    private static String describe(IGenerator generator) {
        StringBuilder builder = new StringBuilder();
        builder.append("location=").append(format(generator.getLocation()));
        builder.append(" delay=").append(generator.getDelay());
        builder.append(" amount=").append(generator.getAmount());
        if (generator.getOre() != null) {
            builder.append(" ore=").append(generator.getOre().getType().name().toLowerCase());
        }
        return builder.toString();
    }

    private static String format(org.bukkit.Location location) {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }
}
