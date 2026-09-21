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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Emergency teleport back to the team base.
 * <p>
 * Activating the item starts a channel. Moving between blocks or taking
 * damage interrupts the channel when configured. When the channel
 * finishes the player warps to the team spawn.
 */
public class EmergencyTeleportHandler implements ModernItemHandler {

    private static final Map<UUID, Channel> channels = new ConcurrentHashMap<>();

    @Override
    public String id() {
        return ModernItems.EMERGENCY_TELEPORT;
    }

    @Override
    public boolean onUse(UseContext context) {
        Player player = context.getPlayer();
        ITeam team = context.getArena().getTeam(player);
        if (team == null) return false;

        Location target = team.getSpawn();
        if (target == null) return false;

        int channelSeconds = QStudioConfig.getInt(context.getArena().getGroup(),
                "modern-items." + id() + ".channel-seconds");
        channels.put(player.getUniqueId(), new Channel(context.getArena().getWorldName(), player.getUniqueId(),
                target, System.currentTimeMillis() + channelSeconds * 1000L,
                player.getLocation()));
        return true;
    }

    public static class ChannelListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onDamage(EntityDamageEvent e) {
            if (!(e.getEntity() instanceof Player)) return;
            Player player = (Player) e.getEntity();
            Channel channel = channels.get(player.getUniqueId());
            if (channel == null) return;
            IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(player);
            if (arena == null) return;
            if (QStudioConfig.getBoolean(arena.getGroup(), "modern-items." + ModernItems.EMERGENCY_TELEPORT + ".cancel-on-damage")) {
                cancel(player, "qstudio.teleport-interrupted");
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onMove(PlayerMoveEvent e) {
            Player player = e.getPlayer();
            Channel channel = channels.get(player.getUniqueId());
            if (channel == null) return;
            if (channel.getStartBlock().getBlock().equals(e.getTo().getBlock())) return;
            IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(player);
            if (arena == null) return;
            if (QStudioConfig.getBoolean(arena.getGroup(), "modern-items." + ModernItems.EMERGENCY_TELEPORT + ".cancel-on-move")) {
                cancel(player, "qstudio.teleport-interrupted");
            }
        }

        /**
         * Channel ticker, runs once per second from the qStudio task.
         */
        public static void tick() {
            long now = System.currentTimeMillis();
            for (Map.Entry<UUID, Channel> entry : channels.entrySet()) {
                Channel channel = entry.getValue();
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !channel.getWorld().equals(player.getWorld().getName())) {
                    channels.remove(entry.getKey());
                    continue;
                }
                if (now >= channel.getFinishAt()) {
                    channels.remove(entry.getKey());
                    player.teleport(channel.getTarget());
                    FX.playSound(player.getLocation(), FX.versioned("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT"));
                    FX.playEffect(player.getLocation(), FX.versioned("PORTAL", "PORTAL", "PORTAL"));
                    continue;
                }
                int remaining = (int) Math.ceil((channel.getFinishAt() - now) / 1000.0);
                BedWars.nms.playAction(player, com.andrei1058.bedwars.api.language.Language.getMsg(player,
                        "qstudio.teleport-channel").replace("{seconds}", String.valueOf(remaining)));
            }
        }
    }

    /**
     * Cancel the channel of a player regardless of configuration (EMP support).
     */
    public static void forceCancel(Player player) {  // static on outer class; exposed on listener too
        cancel(player, "qstudio.teleport-interrupted");
    }

    private static void cancel(Player player, String messagePath) {
        if (channels.remove(player.getUniqueId()) != null) {
            BedWars.nms.playAction(player, com.andrei1058.bedwars.api.language.Language.getMsg(player, messagePath));
        }
    }

    public static void cleanupArena(String world) {
        channels.values().removeIf(channel -> channel.getWorld().equals(world));
    }

    private static class Channel {
        private final String world;
        private final UUID player;
        private final Location target;
        private final long finishAt;
        private final Location startBlock;

        Channel(String world, UUID player, Location target, long finishAt, Location startBlock) {
            this.world = world;
            this.player = player;
            this.target = target;
            this.finishAt = finishAt;
            this.startBlock = startBlock;
        }

        String getWorld() {
            return world;
        }

        Location getTarget() {
            return target;
        }

        long getFinishAt() {
            return finishAt;
        }

        Location getStartBlock() {
            return startBlock;
        }
    }
}
