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

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.configuration.ConfigManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central configuration of the qStudio layer.
 * <p>
 * Values are resolved per arena group through gameplay profiles stored in
 * the profiles/ folder. A profile can override any path of this file.
 * When a group has no profile assigned, the active profile defined in
 * qstudio.yml is used as fallback.
 */
public class QStudioConfig {

    private static final Map<String, GameplayProfile> profilesByGroup = new HashMap<>();
    private static GameplayProfile activeProfile;
    private static ConfigManager config;

    private QStudioConfig() {
    }

    /**
     * Load qstudio.yml and every gameplay profile found in profiles/.
     */
    public static void init() {
        config = new ConfigManager(BedWars.plugin, "qstudio", BedWars.plugin.getDataFolder().getPath());
        YmlDefaults.apply(config.getYml());
        config.getYml().options().copyDefaults(true);
        config.save();

        File folder = new File(BedWars.plugin.getDataFolder(), "profiles");
        if (!folder.exists() && folder.mkdirs()) {
            BedWars.debug("qStudio: created profiles folder");
        }

        profilesByGroup.clear();
        GameplayProfile.register(new GameplayProfile("default", loadProfileYml("default")));
        GameplayProfile.register(new GameplayProfile("classic", loadProfileYml("classic")));
        GameplayProfile.register(new GameplayProfile("competitive", loadProfileYml("competitive")));

        String active = config.getYml().getString("active-profile", "default");
        activeProfile = GameplayProfile.getByName(active);
        if (activeProfile == null) {
            BedWars.plugin.getLogger().warning("qStudio: unknown active-profile '" + active + "', using 'default'.");
            activeProfile = GameplayProfile.getByName("default");
        }

        // group assignments
        if (config.getYml().isSet("profile-groups")) {
            for (String group : config.getYml().getConfigurationSection("profile-groups").getKeys(false)) {
                String profileName = config.getYml().getString("profile-groups." + group);
                GameplayProfile profile = GameplayProfile.getByName(profileName);
                if (profile != null) {
                    profilesByGroup.put(group.toLowerCase(), profile);
                } else {
                    BedWars.plugin.getLogger().warning("qStudio: profile '" + profileName + "' for group '" + group + "' does not exist.");
                }
            }
        }

        if (config.getBoolean("debug")) {
            DebugLog.setEnabled(true);
        }
    }

    private static YamlConfiguration loadProfileYml(String name) {
        File file = new File(new File(BedWars.plugin.getDataFolder(), "profiles"), name + ".yml");
        if (!file.exists()) {
            // create the profile file with documentation so admins can edit it
            YamlConfiguration fresh = new YamlConfiguration();
            fresh.options().header("qStudio gameplay profile: " + name
                    + "\nAny path from qstudio.yml can be overridden here."
                    + "\nAssign profiles to arena groups in qstudio.yml under profile-groups.");
            try {
                fresh.save(file);
            } catch (java.io.IOException ex) {
                BedWars.plugin.getLogger().warning("qStudio: could not create profile " + name + ": " + ex.getMessage());
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * @return the profile used by the given arena group.
     */
    public static GameplayProfile getProfileForGroup(String group) {
        GameplayProfile profile = group == null ? null : profilesByGroup.get(group.toLowerCase());
        return profile == null ? activeProfile : profile;
    }

    public static String getString(String group, String path) {
        GameplayProfile profile = getProfileForGroup(group);
        if (profile != null && profile.getYml().isSet(path)) return profile.getYml().getString(path);
        return config.getYml().getString(path);
    }

    public static int getInt(String group, String path) {
        GameplayProfile profile = getProfileForGroup(group);
        if (profile != null && profile.getYml().isSet(path)) return profile.getYml().getInt(path);
        return config.getYml().getInt(path);
    }

    public static double getDouble(String group, String path) {
        GameplayProfile profile = getProfileForGroup(group);
        if (profile != null && profile.getYml().isSet(path)) return profile.getYml().getDouble(path);
        return config.getYml().getDouble(path);
    }

    public static boolean getBoolean(String group, String path) {
        GameplayProfile profile = getProfileForGroup(group);
        if (profile != null && profile.getYml().isSet(path)) return profile.getYml().getBoolean(path);
        return config.getYml().getBoolean(path);
    }

    public static List<String> getList(String group, String path) {
        GameplayProfile profile = getProfileForGroup(group);
        if (profile != null && profile.getYml().isSet(path)) return profile.getYml().getStringList(path);
        return config.getYml().getStringList(path);
    }

    /**
     * Raw access for the validator and admin tooling.
     */
    public static ConfigManager getConfig() {
        return config;
    }

    /**
     * Re-read profile assignments and profile files.
     */
    public static void reload() {
        init();
    }
}
