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
import com.andrei1058.bedwars.qstudio.modern.FX;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

/**
 * Configurable behaviour for the vanilla projectiles used in bed wars:
 * arrows, snowballs and eggs. Damage and knockback multipliers are read
 * from combat.projectiles.* so servers can balance them per profile.
 */
public class ProjectileEnhancer implements Listener {

    private static String sectionOf(Projectile projectile) {
        if (projectile instanceof Arrow) return "arrow";
        if (projectile instanceof Snowball) return "snowball";
        if (projectile instanceof Egg) return "egg";
        return null;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Projectile)) return;
        Projectile projectile = (Projectile) e.getDamager();
        if (!(projectile.getShooter() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer((Player) e.getEntity());
        if (arena == null) return;

        String section = sectionOf(projectile);
        if (section == null) return;
        if (!QStudioConfig.getBoolean(arena.getGroup(), "combat.projectiles." + section + ".enabled")) return;

        double damage = QStudioConfig.getDouble(arena.getGroup(), "combat.projectiles." + section + ".damage");
        if (damage > 0) {
            e.setDamage(damage);
        }
        double knockback = QStudioConfig.getDouble(arena.getGroup(),
                "combat.projectiles." + section + ".knockback-multiplier");
        if (knockback != 1.0) {
            // scale the impact direction knockback
            var velocity = e.getEntity().getVelocity();
            var direction = projectile.getVelocity().normalize().multiply(0.6 * knockback);
            direction.setY(0.35 * knockback);
            e.getEntity().setVelocity(direction.add(new org.bukkit.util.Vector(velocity.getX() * 0.3, 0, velocity.getZ() * 0.3)));
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();
        String section = sectionOf(projectile);
        if (section == null) return;
        if (!(projectile.getShooter() instanceof Player)) return;
        IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer((Player) projectile.getShooter());
        if (arena == null) return;
        if (!QStudioConfig.getBoolean(arena.getGroup(), "combat.projectiles." + section + ".enabled")) return;

        String effect = QStudioConfig.getString(arena.getGroup(), "combat.projectiles." + section + ".hit-effect");
        if (effect != null && !effect.isEmpty()) {
            FX.playEffect(projectile.getLocation(), effect);
        }
        String sound = QStudioConfig.getString(arena.getGroup(), "combat.projectiles." + section + ".hit-sound");
        if (sound != null && !sound.isEmpty()) {
            FX.playSound(projectile.getLocation(), sound);
        }
    }
}
