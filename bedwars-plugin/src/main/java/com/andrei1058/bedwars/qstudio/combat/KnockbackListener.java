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

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configurable knockback profiles.
 * <p>
 * Profiles define separate horizontal and vertical multipliers for
 * grounded, airborne, sprinting, projectile and explosion knockback.
 * The profile used by an arena group is selected through
 * combat.knockback-profile in qstudio.yml or a gameplay profile.
 */
public class KnockbackListener implements Listener {

    /** victim -> velocity to apply on the next tick. */
    private final Map<UUID, Vector> queuedVelocities = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player victim = (Player) e.getEntity();
        IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(victim);
        if (arena == null || arena.getStatus() != com.andrei1058.bedwars.api.arena.GameState.playing) return;

        String group = arena.getGroup();
        String profileName = QStudioConfig.getString(group, "combat.knockback-profile");
        if (profileName == null || profileName.isEmpty()) profileName = "default";
        String base = "combat.knockback-profiles." + profileName + ".";

        boolean projectile = e.getDamager() instanceof org.bukkit.entity.Projectile;
        double horizontal = QStudioConfig.getDouble(group, base + (projectile ? "projectile-multiplier" : "ground-horizontal"));
        double vertical = QStudioConfig.getDouble(group, base + "ground-vertical");

        if (!victim.isOnGround()) {
            horizontal = QStudioConfig.getDouble(group, base + "air-horizontal");
            vertical = QStudioConfig.getDouble(group, base + "air-vertical");
        }

        if (!projectile && e.getDamager() instanceof Player) {
            Player attacker = (Player) e.getDamager();
            if (attacker.isSprinting()) {
                horizontal += QStudioConfig.getDouble(group, base + "sprint-bonus");
            }
        }

        if (horizontal == 1.0 && vertical == 1.0) return;

        // let vanilla compute the knockback first, then scale it on the next tick
        final double h = horizontal;
        final double v = vertical;
        final UUID victimId = victim.getUniqueId();
        queuedVelocities.put(victimId, null);
        Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
            Vector velocity = victim.getVelocity();
            queuedVelocities.remove(victimId);
            victim.setVelocity(new Vector(velocity.getX() * h, velocity.getY() * v, velocity.getZ() * h));
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player victim = (Player) e.getEntity();
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                && e.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) return;
        IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(victim);
        if (arena == null) return;

        String group = arena.getGroup();
        String profileName = QStudioConfig.getString(group, "combat.knockback-profile");
        if (profileName == null || profileName.isEmpty()) profileName = "default";
        double multiplier = QStudioConfig.getDouble(group,
                "combat.knockback-profiles." + profileName + ".explosion-multiplier");
        if (multiplier != 1.0) {
            e.setDamage(e.getDamage() * multiplier);
        }
    }
}
