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
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.qstudio.ModernItems;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporary team shield.
 * <p>
 * While the shield of a team is active its members receive reduced
 * melee and projectile damage. Only one shield can be active per team
 * and enemies can purge it with the EMP pulse.
 */
public class BaseShieldHandler implements ModernItemHandler {

    /** world -> team name -> expiry (millis). */
    private static final Map<String, Map<String, Long>> activeShields = new ConcurrentHashMap<>();

    @Override
    public String id() {
        return ModernItems.BASE_SHIELD;
    }

    @Override
    public boolean onUse(UseContext context) {
        Player player = context.getPlayer();
        ITeam team = context.getArena().getTeam(player);
        if (team == null) return false;

        int duration = QStudioConfig.getInt(context.getArena().getGroup(),
                "modern-items." + id() + ".duration-seconds");
        activeShields
                .computeIfAbsent(context.getArena().getWorldName(), k -> new ConcurrentHashMap<>())
                .put(team.getName(), System.currentTimeMillis() + duration * 1000L);

        for (Player member : team.getMembers()) {
            BedWars.plugin.nms.playAction(member, com.andrei1058.bedwars.api.language.Language.getMsg(member,
                    "qstudio.shield-activated").replace("{seconds}", String.valueOf(duration)));
        }
        FX.playSound(team.getSpawn(), FX.versioned("GLASS_BREAK", "BLOCK_GLASS_BREAK", "BLOCK_GLASS_BREAK"));
        return true;
    }

    public static boolean isShielded(String world, String team) {
        Map<String, Long> shields = activeShields.get(world);
        if (shields == null) return false;
        Long expiry = shields.get(team);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    public static class ShieldListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onDamage(EntityDamageByEntityEvent e) {
            if (!(e.getEntity() instanceof Player)) return;
            Player victim = (Player) e.getEntity();
            IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(victim);
            if (arena == null) return;
            ITeam team = arena.getTeam(victim);
            if (team == null) return;
            if (!isShielded(arena.getWorldName(), team.getName())) return;

            String group = arena.getGroup();
            boolean projectile = e.getDamager() instanceof org.bukkit.entity.Projectile;
            double reduction = QStudioConfig.getDouble(group, "modern-items." + ModernItems.BASE_SHIELD + "."
                    + (projectile ? "projectile-reduction" : "damage-reduction"));
            if (reduction > 0 && reduction < 1) {
                e.setDamage(e.getDamage() * (1 - reduction));
            }
        }
    }

    /**
     * Expire finished shields, runs once per second.
     */
    public static void tick() {
        long now = System.currentTimeMillis();
        for (Map<String, Long> shields : activeShields.values()) {
            shields.values().removeIf(expiry -> expiry <= now);
        }
    }

    /**
     * Purge the shield of a team (EMP support).
     */
    public static void removeShield(String world, String team) {
        Map<String, Long> shields = activeShields.get(world);
        if (shields != null) {
            shields.remove(team);
        }
    }

    public static void cleanupArena(String world) {
        activeShields.remove(world);
    }
}
