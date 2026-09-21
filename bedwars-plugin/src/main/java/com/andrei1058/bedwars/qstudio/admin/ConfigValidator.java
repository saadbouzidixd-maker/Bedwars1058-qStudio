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

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import com.andrei1058.bedwars.shop.ShopManager;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates the qStudio configuration and the shop/upgrade references:
 * materials, prices, slots and cross references. Returns human readable
 * problems instead of failing silently at runtime.
 */
public final class ConfigValidator {

    private ConfigValidator() {
    }

    public static List<String> validate() {
        List<String> problems = new ArrayList<>();
        validateModernItems(problems);
        validateShop(problems);
        return problems;
    }

    private static void validateModernItems(List<String> problems) {
        var yml = QStudioConfig.getConfig().getYml();
        if (!yml.isSet("modern-items")) return;
        for (String id : yml.getConfigurationSection("modern-items").getKeys(false)) {
            String material = yml.getString("modern-items." + id + ".display-material", "");
            if (!material.isEmpty() && Material.matchMaterial(material) == null) {
                problems.add("modern-items." + id + ".display-material: unknown material '" + material + "'");
            }
            if (yml.getInt("modern-items." + id + ".cooldown-seconds", 0) < 0) {
                problems.add("modern-items." + id + ".cooldown-seconds: cannot be negative");
            }
        }
    }

    private static void validateShop(List<String> problems) {
        if (ShopManager.getShop() == null) {
            problems.add("shop: shop did not load");
            return;
        }
        if (ShopManager.getShop().getCategoryList().isEmpty()) {
            problems.add("shop: no categories were loaded");
        }
        for (ShopCategory category : ShopManager.getShop().getCategoryList()) {
            if (category.getSlot() < 0 || category.getSlot() > 53) {
                problems.add("shop category " + category.getName() + ": slot out of range");
            }
            for (ICategoryContent content : category.getCategoryContentList()) {
                if (content.getSlot() < 0 || content.getSlot() > 53) {
                    problems.add("shop content " + content.getIdentifier() + ": slot out of range");
                }
            }
        }
    }
}
