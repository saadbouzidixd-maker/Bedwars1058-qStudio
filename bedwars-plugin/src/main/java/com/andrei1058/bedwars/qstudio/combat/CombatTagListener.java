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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.andrei1058.bedwars.BedWars.nms;

/**
 * Optional combat tagging.
 * <p>
 * While tagged players see a countdown on the action bar and can be
 * blocked from using ender pearls when configured.
 */
public class CombatTagListener implements Listener {

    /** player -> tag expiry (millis). */
    private static final Map<UUID, Long> tags = new ConcurrentHashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) tag((Player) e.getDamager());
        if (e.getEntity() instanceof Player) tag((Player) e.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPearl(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getItem().getType() != Material.ENDER_PEARL) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = e.getPlayer();
        if (isTagged(player)) {
            IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(player);
            if (arena == null) return;
            if (QStudioConfig.getBoolean(arena.getGroup(), "combat.combat-tag.block-ender-pearl")) {
                e.setCancelled(true);
                nms.playAction(player, org.bukkit.ChatColor.RED + com.andrei1058.bedwars.api.language.Language.getMsg(
                        player, "qstudio.combat-tag-pearl"));
            }
        }
    }

    private static void tag(Player player) {
        IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(player);
        if (arena == null) return;
        if (!QStudioConfig.getBoolean(arena.getGroup(), "combat.combat-tag.enabled")) return;
        int duration = QStudioConfig.getInt(arena.getGroup(), "combat.combat-tag.duration-seconds");
        tags.put(player.getUniqueId(), System.currentTimeMillis() + duration * 1000L);
    }

    public static boolean isTagged(Player player) {
        Long expiry = tags.get(player.getUniqueId());
        return expiry != null && expiry > System.currentTimeMillis();
    }

    /**
     * Refresh action bar countdowns, runs once per second.
     */
    public static void tick() {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Long> entry : tags.entrySet()) {
            long remaining = entry.getValue() - now;
            if (remaining <= 0) continue;
            Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;
            IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(player);
            if (arena == null) continue;
            String template = QStudioConfig.getString(arena.getGroup(), "combat.combat-tag.action-bar");
            if (template != null && !template.isEmpty()) {
                nms.playAction(player, org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        template.replace("{seconds}", String.valueOf((int) Math.ceil(remaining / 1000.0)))));
            }
        }
        tags.values().removeIf(expiry -> expiry <= now);
    }

    public static void cleanup() {
        tags.clear();
    }
}
