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
import com.andrei1058.bedwars.api.qstudio.ModernItems;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Short range dash item with a configurable amount of uses.
 * The remaining uses are stored on the item stack itself so the item can
 * be dropped, picked up and traded without losing state.
 */
public class DashHandler implements ModernItemHandler {

    private static final String USES_TAG = "qstudioUses";

    @Override
    public String id() {
        return ModernItems.DASH;
    }

    @Override
    public boolean onUse(UseContext context) {
        Player player = context.getPlayer();
        var group = context.getArena().getGroup();
        double strength = QStudioConfig.getDouble(group, "modern-items." + id() + ".strength");

        player.setVelocity(player.getLocation().getDirection().normalize().multiply(strength).setY(0.15));
        FX.playEffect(player.getLocation(), FX.versioned("SMOKE", "SMOKE", "SMOKE"));
        FX.playSound(player.getLocation(), FX.versioned("GHAST_FIREBALL", "ENTITY_GHAST_SHOOT", "ENTITY_GHAST_SHOOT"));

        int uses = getUses(context.getItem());
        if (uses <= 1) {
            ModernItemManager.consumeOne(player, context.getItem());
        } else {
            setUses(player, context.getItem(), uses - 1);
        }
        return true;
    }

    /**
     * Write the initial amount of uses on a freshly created dash item.
     */
    public static void initUses(Player player, org.bukkit.inventory.ItemStack item, int uses) {
        ItemStack tagged = com.andrei1058.bedwars.BedWars.plugin.nms.setShopUpgradeIdentifier(item,
                ModernItemManager.getRawTag(item) + ":" + uses);
        // replace the held stack with the tagged one
        player.getInventory().setItemInHand(tagged);
        player.updateInventory();
    }

    private static int getUses(org.bukkit.inventory.ItemStack item) {
        String raw = ModernItemManager.getRawTag(item);
        int separator = raw.lastIndexOf(':');
        if (separator < 0) return 1;
        try {
            return Integer.parseInt(raw.substring(separator + 1));
        } catch (Exception ex) {
            return 1;
        }
    }

    private static void setUses(Player player, org.bukkit.inventory.ItemStack item, int uses) {
        String raw = ModernItemManager.getRawTag(item);
        int separator = raw.lastIndexOf(':');
        String base = separator < 0 ? raw : raw.substring(0, separator);
        org.bukkit.inventory.ItemStack tagged = com.andrei1058.bedwars.BedWars.plugin.nms.setShopUpgradeIdentifier(item, base + ":" + uses);
        player.getInventory().setItemInHand(tagged);
        player.updateInventory();
    }
}
