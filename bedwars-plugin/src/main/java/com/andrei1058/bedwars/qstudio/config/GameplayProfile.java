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

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * A gameplay profile is a named set of qStudio configuration overrides.
 */
public class GameplayProfile {

    private static final Map<String, GameplayProfile> profilesByName = new HashMap<>();

    private final String name;
    private final YamlConfiguration yml;

    public GameplayProfile(String name, YamlConfiguration yml) {
        this.name = name.toLowerCase();
        this.yml = yml;
    }

    public static void register(GameplayProfile profile) {
        profilesByName.put(profile.getName(), profile);
    }

    public static GameplayProfile getByName(String name) {
        return name == null ? null : profilesByName.get(name.toLowerCase());
    }

    public String getName() {
        return name;
    }

    public YamlConfiguration getYml() {
        return yml;
    }
}
