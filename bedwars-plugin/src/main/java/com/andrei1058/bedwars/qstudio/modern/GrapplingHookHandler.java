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
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.util.Vector;

/**
 * Grappling hook on legacy servers using a fishing rod display item.
 * Pulls the player towards the targeted block, or towards an enemy when
 * enemy pulling is enabled.
 */
public class GrapplingHookHandler implements ModernItemHandler, Listener {

    @Override
    public String id() {
        return ModernItems.GRAPPLING_HOOK;
    }

    @Override
    public boolean onUse(UseContext context) {
        Player player = context.getPlayer();
        var group = context.getArena().getGroup();
        double maxRange = QStudioConfig.getDouble(group, "modern-items." + id() + ".max-range");
        double pullStrength = QStudioConfig.getDouble(group, "modern-items." + id() + ".pull-strength");
        boolean pullEnemies = QStudioConfig.getBoolean(group, "modern-items." + id() + ".pull-enemies");

        if (pullEnemies) {
            Player target = rayTracePlayer(player, maxRange);
            if (target != null) {
                Vector pull = player.getLocation().toVector()
                        .subtract(target.getLocation().toVector()).normalize().multiply(pullStrength * 0.7);
                target.setVelocity(pull.setY(pullStrength * 0.4));
                FX.playSound(target.getLocation(), FX.versioned("MAGMACUBE_JUMP", "ENTITY_MAGMA_CUBE_JUMP", "ENTITY_MAGMA_CUBE_JUMP"));
                return false;
            }
        }

        Location target = rayTraceBlock(player, maxRange);
        if (target == null) {
            BedWars.nms.playAction(player, com.andrei1058.bedwars.api.language.Language.getMsg(player,
                    "qstudio.grapple-too-far"));
            return false;
        }

        Vector direction = target.toVector().subtract(player.getLocation().toVector()).normalize();
        player.setVelocity(direction.multiply(pullStrength).setY(pullStrength * 0.35));
        FX.playEffect(player.getLocation(), FX.versioned("CRIT", "CRIT", "CRIT"));
        FX.playSound(player.getLocation(), FX.versioned("MAGMACUBE_JUMP", "ENTITY_MAGMA_CUBE_JUMP", "ENTITY_MAGMA_CUBE_JUMP"));
        return false;
    }

    /**
     * Rods still fire the cast event on some versions, keep them silent.
     */
    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        if (ModernItemManager.getItemId(e.getPlayer().getItemInHand()) != null) {
            e.setCancelled(true);
        }
    }

    private static Location rayTraceBlock(Player player, double maxRange) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();
        for (double step = 1; step <= maxRange; step += 0.5) {
            Location point = eye.clone().add(direction.clone().multiply(step));
            if (point.getBlock().getType().isSolid()) {
                return point;
            }
        }
        return null;
    }

    private static Player rayTracePlayer(Player player, double maxRange) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();
        for (double step = 1; step <= maxRange; step += 0.5) {
            Location point = eye.clone().add(direction.clone().multiply(step));
            for (Entity nearbyEntity : point.getWorld().getNearbyEntities(point, 1.2, 1.2, 1.2)) {
                if (nearbyEntity instanceof Player && nearbyEntity != player) {
                    return (Player) nearbyEntity;
                }
            }
        }
        return null;
    }
}
