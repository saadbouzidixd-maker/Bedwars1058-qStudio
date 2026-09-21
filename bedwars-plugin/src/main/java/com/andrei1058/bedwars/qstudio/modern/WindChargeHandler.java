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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wind charge behaviour for legacy servers.
 * <p>
 * Display item is a snow ball. Throwing it launches a charge projectile
 * that pushes entities away from the impact with a strong vertical lift,
 * replicating the modern wind burst mechanic.
 */
public class WindChargeHandler implements ModernItemHandler {

    private static final Map<UUID, Long> activeCharges = new ConcurrentHashMap<>();

    @Override
    public String id() {
        return ModernItems.WIND_CHARGE;
    }

    @Override
    public boolean onUse(UseContext context) {
        Player player = context.getPlayer();
        Snowball charge = player.launchProjectile(Snowball.class);
        charge.setVelocity(player.getLocation().getDirection().multiply(1.7));
        activeCharges.put(charge.getUniqueId(), System.currentTimeMillis());
        FX.playSound(player.getLocation(), FX.versioned("ENDERDRAGON_WINGS", "ENTITY_ENDER_DRAGON_FLAP", "ENTITY_ENDER_DRAGON_FLAP"));
        // consume one charge from the stack
        ModernItemManager.consumeOne(player, context.getItem());
        return true;
    }

    public static class ChargeListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onLaunch(ProjectileLaunchEvent e) {
            if (!(e.getEntity() instanceof Snowball)) return;
            Projectile projectile = e.getEntity();
            if (!(projectile.getShooter() instanceof Player)) return;
            Player shooter = (Player) projectile.getShooter();
            if (ModernItemManager.getItemId(shooter.getItemInHand()) != null) {
                activeCharges.put(projectile.getUniqueId(), System.currentTimeMillis());
            }
        }

        @EventHandler
        public void onHit(ProjectileHitEvent e) {
            Projectile projectile = e.getEntity();
            if (activeCharges.remove(projectile.getUniqueId()) == null) return;
            if (!(projectile.getShooter() instanceof Player)) return;
            Player shooter = (Player) projectile.getShooter();
            IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(shooter);
            if (arena == null) return;
            String group = arena.getGroup();

            double knockback = QStudioConfig.getDouble(group, "modern-items." + ModernItems.WIND_CHARGE + ".knockback");
            double vertical = QStudioConfig.getDouble(group, "modern-items." + ModernItems.WIND_CHARGE + ".vertical-launch");
            double horizontal = QStudioConfig.getDouble(group, "modern-items." + ModernItems.WIND_CHARGE + ".horizontal-launch");
            double damage = QStudioConfig.getDouble(group, "modern-items." + ModernItems.WIND_CHARGE + ".damage");

            var hitLocation = projectile.getLocation();
            for (Entity entity : hitLocation.getWorld().getNearbyEntities(hitLocation, 3.5, 3, 3.5)) {
                if (!(entity instanceof Player)) continue;
                Player target = (Player) entity;
                if (arena.isSpectator(target)) continue;

                if (damage > 0 && !target.equals(shooter)) {
                    target.damage(damage, shooter);
                }

                Vector away = target.getLocation().toVector().subtract(hitLocation.toVector());
                away.setY(0);
                if (away.lengthSquared() < 0.01) {
                    away = new Vector(0, 0, 0);
                } else {
                    away.normalize();
                }
                double multiplier = target.equals(shooter) ? 1.0 : knockback;
                Vector velocity = away.multiply(horizontal * multiplier).setY(vertical * multiplier);
                target.setVelocity(velocity);
            }

            FX.playEffect(hitLocation, FX.versioned("CLOUD", "CLOUD", "CLOUD"));
            FX.playSound(hitLocation, FX.versioned("GHAST_FIREBALL", "ENTITY_GHAST_SHOOT", "ENTITY_GHAST_SHOOT"));
        }

        /**
         * Called by the arena cleanup to drop projectile references.
         */
        public static void cleanup() {
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<UUID, Long>> it = activeCharges.entrySet().iterator();
            while (it.hasNext()) {
                if (now - it.next().getValue() > 60_000) it.remove();
            }
        }
    }
}
