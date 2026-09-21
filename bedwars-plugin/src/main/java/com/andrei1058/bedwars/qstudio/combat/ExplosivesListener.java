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

package com.andrei1058.bedwars.qstudio.combat;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configurable explosives.
 * <p>
 * Fireball and TNT explosion power, damage and knockback are read from
 * the qStudio configuration. Players that were launched by an explosion
 * receive a configurable damage reduction to enable TNT jumping.
 */
public class ExplosivesListener implements Listener {

    /** players recently launched by an explosion. */
    private static final Map<UUID, Long> launched = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrime(org.bukkit.event.entity.ExplosionPrimeEvent e) {
        IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByIdentifier(
                e.getEntity().getLocation().getWorld().getName());
        if (arena == null) return;
        String group = arena.getGroup();

        float radius = e.getEntity() instanceof org.bukkit.entity.Fireball
                ? (float) QStudioConfig.getDouble(group, "explosives.fireball.explosion-radius")
                : (float) QStudioConfig.getDouble(group, "explosives.tnt.explosion-power");
        if (radius > 0) {
            e.setRadius(radius);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByIdentifier(e.getLocation().getWorld().getName());
        if (arena == null) return;
        String group = arena.getGroup();

        int blockDamage = e.getEntity() instanceof TNTPrimed
                ? 100
                : QStudioConfig.getInt(group, "explosives.fireball.block-damage-percent");
        if (blockDamage <= 0) {
            e.blockList().clear();
        } else if (blockDamage < 100) {
            e.blockList().removeIf(block -> java.util.concurrent.ThreadLocalRandom.current().nextInt(100) >= blockDamage);
        }

        // remember nearby players so their fall damage can be reduced (TNT jump)
        double launchRadius = e.getYield() * 1.5F + 3;
        for (org.bukkit.entity.Entity entity : e.getLocation().getWorld()
                .getNearbyEntities(e.getLocation(), launchRadius, launchRadius, launchRadius)) {
            if (entity instanceof Player) {
                launched.put(entity.getUniqueId(), System.currentTimeMillis() + 3000);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        Long until = launched.get(player.getUniqueId());
        if (until == null || until < System.currentTimeMillis()) return;

        IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(player);
        if (arena == null) return;
        if (!QStudioConfig.getBoolean(arena.getGroup(), "explosives.tnt.jump-enabled")) return;

        double reduction = QStudioConfig.getDouble(arena.getGroup(), "explosives.tnt.jump-damage-reduction");
        if (reduction > 0) {
            e.setDamage(e.getDamage() * (1 - Math.min(1, reduction)));
        }
    }

    /**
     * Drop stale launch markers, runs once per minute.
     */
    public static void cleanup() {
        long now = System.currentTimeMillis();
        launched.values().removeIf(expiry -> expiry < now);
    }
}
