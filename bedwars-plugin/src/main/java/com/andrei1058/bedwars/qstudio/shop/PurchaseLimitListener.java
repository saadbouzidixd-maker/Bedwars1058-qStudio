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

package com.andrei1058.bedwars.qstudio.shop;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import com.andrei1058.bedwars.api.language.Language;
import org.bukkit.entity.Player;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shop purchase limits: per life, per game and a purchase cooldown.
 * Counters are keyed by the shop content identifier and reset on death
 * (per life) or on arena leave (per game).
 */
public class PurchaseLimitListener implements Listener {

    /** player -> identifier -> per game count. */
    private static final Map<UUID, Map<String, Integer>> perGame = new ConcurrentHashMap<>();
    /** player -> identifier -> per life count. */
    private static final Map<UUID, Map<String, Integer>> perLife = new ConcurrentHashMap<>();
    /** player -> identifier -> last purchase. */
    private static final Map<UUID, Map<String, Long>> lastPurchase = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBuy(ShopBuyEvent e) {
        if (!QStudioConfig.getBoolean(e.getArena() == null ? null : e.getArena().getGroup(),
                "shop.purchase-limits.enabled")) return;

        Player player = e.getBuyer();
        IArena arena = e.getArena();
        if (arena == null) return;

        String identifier = e.getCategoryContent() == null ? null : e.getCategoryContent().getIdentifier();
        if (identifier == null) return;

        String group = arena.getGroup();
        String base = "shop.purchase-limits.";

        long now = System.currentTimeMillis();
        int cooldown = QStudioConfig.getInt(group, base + "cooldown-seconds");
        if (cooldown > 0) {
            Long last = lastPurchase.getOrDefault(player.getUniqueId(), new ConcurrentHashMap<>()).get(identifier);
            if (last != null && now - last < cooldown * 1000L) {
                player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        Language.getMsg(player, "qstudio.purchase-cooldown")));
                e.setCancelled(true);
                return;
            }
        }

        int lifeLimit = QStudioConfig.getInt(group, base + "default-per-life");
        int gameLimit = QStudioConfig.getInt(group, base + "default-per-game");

        if (lifeLimit >= 0) {
            int count = perLife.getOrDefault(player.getUniqueId(), new ConcurrentHashMap<>())
                    .getOrDefault(identifier, 0);
            if (count >= lifeLimit) {
                player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        Language.getMsg(player, "qstudio.purchase-limit-life")));
                e.setCancelled(true);
                return;
            }
        }
        if (gameLimit >= 0) {
            int count = perGame.getOrDefault(player.getUniqueId(), new ConcurrentHashMap<>())
                    .getOrDefault(identifier, 0);
            if (count >= gameLimit) {
                player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        Language.getMsg(player, "qstudio.purchase-limit-game")));
                e.setCancelled(true);
                return;
            }
        }

        perLife.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .merge(identifier, 1, Integer::sum);
        perGame.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .merge(identifier, 1, Integer::sum);
        lastPurchase.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(identifier, now);
    }

    @EventHandler
    public void onRespawn(PlayerReSpawnEvent e) {
        perLife.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoinArena(PlayerJoinArenaEvent e) {
        perLife.remove(e.getPlayer().getUniqueId());
        perGame.remove(e.getPlayer().getUniqueId());
        lastPurchase.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onLeave(PlayerLeaveArenaEvent e) {
        perLife.remove(e.getPlayer().getUniqueId());
        perGame.remove(e.getPlayer().getUniqueId());
        lastPurchase.remove(e.getPlayer().getUniqueId());
    }
}
