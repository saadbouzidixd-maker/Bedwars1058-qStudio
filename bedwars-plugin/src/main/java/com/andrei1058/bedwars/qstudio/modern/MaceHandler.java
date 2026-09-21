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
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.qstudio.ModernItems;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * Mace behaviour on versions without the native item.
 * <p>
 * Display item is a configured legacy material (iron shovel on old
 * releases). Hitting while falling performs a smash: bonus damage scaled
 * by the fall, strong knockback and an area push around the victim.
 */
public class MaceHandler implements ModernItemHandler {

    private static final String SMASH_COOLDOWN = "mace-smash";

    @Override
    public String id() {
        return ModernItems.MACE;
    }

    @Override
    public boolean onUse(UseContext context) {
        // melee weapon: activation happens through the hit listener
        return false;
    }

    public static class HitListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onHit(EntityDamageByEntityEvent e) {
            if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) return;
            Player attacker = (Player) e.getDamager();
            Player victim = (Player) e.getEntity();
            IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(attacker);
            if (arena == null || arena != com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(victim)) return;
            if (arena.getStatus() != com.andrei1058.bedwars.api.arena.GameState.playing) return;

            ItemStackProbe probe = new ItemStackProbe(attacker);
            if (!probe.isModern(ModernItems.MACE)) return;

            String group = arena.getGroup();
            double damage = QStudioConfig.getDouble(group, "modern-items." + ModernItems.MACE + ".damage");
            if (damage > 0) {
                e.setDamage(damage);
            }

            double velocityY = attacker.getVelocity().getY();
            boolean falling = velocityY < -0.4;
            double multiplier = QStudioConfig.getDouble(group, "modern-items." + ModernItems.MACE + ".falling-multiplier");
            if (falling && multiplier > 1.0) {
                e.setDamage(e.getDamage() * multiplier);
            }

            if (falling && !ModernItemManager.isOnCooldown(attacker, arena, SMASH_COOLDOWN)) {
                int cooldown = QStudioConfig.getInt(group, "modern-items." + ModernItems.MACE + ".cooldown-seconds");
                if (cooldown > 0) {
                    ModernItemManager.setCooldown(attacker, arena, SMASH_COOLDOWN, cooldown);
                }
                smash(attacker, victim, arena, group);
            }
        }

        private void smash(Player attacker, Player victim, IArena arena, String group) {
            double knockback = QStudioConfig.getDouble(group, "modern-items." + ModernItems.MACE + ".smash-knockback");
            double vertical = QStudioConfig.getDouble(group, "modern-items." + ModernItems.MACE + ".smash-vertical");

            Vector direction = victim.getLocation().toVector()
                    .subtract(attacker.getLocation().toVector()).normalize();
            direction.setY(0);
            if (direction.lengthSquared() > 0.01) {
                victim.setVelocity(direction.multiply(knockback).setY(vertical));
            }

            // area push around the impact point
            for (org.bukkit.entity.Entity nearbyEntity : victim.getWorld().getNearbyEntities(victim.getLocation(), 2.5, 1.5, 2.5)) {
                if (!(nearbyEntity instanceof Player)) continue;
                Player nearby = (Player) nearbyEntity;
                if (nearby == attacker || nearby == victim) continue;
                if (arena.isSpectator(nearby)) continue;
                if (arena.getTeam(attacker) != null && arena.getTeam(attacker).isMember(nearby)) continue;
                Vector push = nearby.getLocation().toVector()
                        .subtract(victim.getLocation().toVector()).normalize().multiply(knockback * 0.6);
                push.setY(vertical * 0.7);
                nearby.setVelocity(push);
            }

            if (QStudioConfig.getBoolean(group, "modern-items." + ModernItems.MACE + ".particles")) {
                FX.playEffect(victim.getLocation(), FX.versioned("CRIT", "CRIT", "CRIT"));
                FX.playEffect(victim.getLocation().add(0, 1, 0), FX.versioned("EXPLOSION", "EXPLOSION", "EXPLOSION"));
            }
            FX.playSound(victim.getLocation(), FX.versioned("ANVIL_LAND", "ENTITY_ANVIL_LAND", "ENTITY_ANVIL_LAND"));
        }
    }

    /**
     * Utility that checks the item in the main hand of a player.
     */
    static class ItemStackProbe {
        private final String modernId;

        ItemStackProbe(Player player) {
            this.modernId = ModernItemManager.getItemId(player.getItemInHand());
        }

        boolean isModern(String id) {
            return id.equals(modernId);
        }
    }
}
