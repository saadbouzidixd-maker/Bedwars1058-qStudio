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

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.qstudio.ModernItems;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Automatic bridge builder.
 * <p>
 * Extends the ground under the player towards the direction they look,
 * with void prevention: it stops when there is nothing left to support
 * or when the configured distance was reached.
 */
public class BridgeBuilderHandler implements ModernItemHandler {

    @Override
    public String id() {
        return ModernItems.BRIDGE_BUILDER;
    }

    @Override
    public boolean onUse(UseContext context) {
        Player player = context.getPlayer();
        IArena arena = context.getArena();
        String group = arena.getGroup();

        int maxDistance = QStudioConfig.getInt(group, "modern-items." + id() + ".max-distance");

        var direction = player.getLocation().getDirection();
        direction.setY(0);
        if (direction.lengthSquared() < 0.01) return false;
        direction.normalize();

        // use the team wool colour like the temporary bridge does
        Material material;
        var team = arena.getTeam(player);
        if (team != null) {
            try {
                material = Material.valueOf(team.getColor().name() + "_WOOL");
            } catch (IllegalArgumentException ex) {
                material = Material.matchMaterial(QStudioConfig.getString(group,
                        "modern-items." + id() + ".display-material"));
            }
        } else {
            material = Material.matchMaterial(QStudioConfig.getString(group,
                    "modern-items." + id() + ".display-material"));
        }
        if (material == null) material = Material.WOOL;

        Block feet = player.getLocation().getBlock();
        int placed = 0;
        int buildHeight = arena.getConfig().getInt("maxBuildHeight");
        for (int step = 1; step <= maxDistance; step++) {
            Block next = feet.getLocation().clone().add(direction.clone().multiply(step)).getBlock();
            Block below = next.getRelative(org.bukkit.block.BlockFace.DOWN);

            // void prevention: stop extending when the platform would float over nothing
            if (next.getType() == Material.AIR && below.getType() == Material.AIR) break;
            if (next.getType() != Material.AIR) continue;
            if (buildHeight > 0 && next.getY() > buildHeight) break;

            next.setType(material);
            placed++;
        }

        if (placed > 0) {
            FX.playEffect(player.getLocation(), FX.versioned("GLASS_BREAK", "BLOCK_GLASS_BREAK", "BLOCK_GLASS_BREAK"));
            FX.playSound(player.getLocation(), FX.versioned("DIG_WOOL", "BLOCK_WOOL_PLACE", "BLOCK_WOOL_PLACE"));
            return true;
        }
        return false;
    }
}
