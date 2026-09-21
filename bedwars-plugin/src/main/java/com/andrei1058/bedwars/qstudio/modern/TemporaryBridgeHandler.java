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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporary bridge item.
 * <p>
 * Builds a bridge in the horizontal direction the player is looking.
 * Every placed block is owned by the builder team and fades after the
 * configured lifetime.
 */
public class TemporaryBridgeHandler implements ModernItemHandler {

    /** world -> block key -> expiry (millis). */
    private static final Map<String, Map<String, Long>> tempBlocks = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Material>> previousBlocks = new ConcurrentHashMap<>();

    @Override
    public String id() {
        return ModernItems.TEMPORARY_BRIDGE;
    }

    @Override
    public boolean onUse(UseContext context) {
        Player player = context.getPlayer();
        IArena arena = context.getArena();
        String group = arena.getGroup();

        int maxLength = QStudioConfig.getInt(group, "modern-items." + ModernItems.TEMPORARY_BRIDGE + ".max-length");
        int maxBlocks = QStudioConfig.getInt(group, "modern-items." + ModernItems.TEMPORARY_BRIDGE + ".max-blocks");
        int lifetime = QStudioConfig.getInt(group, "modern-items." + ModernItems.TEMPORARY_BRIDGE + ".lifetime-seconds");

        Material material = bridgeMaterial(player, arena, group);

        var direction = player.getLocation().getDirection();
        direction.setY(0);
        if (direction.lengthSquared() < 0.01) return false;
        direction.normalize();

        var start = player.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.DOWN);
        int placed = 0;
        for (int step = 1; step <= maxLength && placed < maxBlocks; step++) {
            Block target = start.getLocation().clone()
                    .add(direction.clone().multiply(step)).getBlock();
            if (target.getType() != Material.AIR) continue;
            if (target.getY() < 0 || target.getY() > target.getWorld().getMaxHeight() - 1) continue;

            previousBlocks
                    .computeIfAbsent(arena.getWorldName(), k -> new ConcurrentHashMap<>())
                    .put(key(target), target.getType());
            target.setType(material);
            tempBlocks
                    .computeIfAbsent(arena.getWorldName(), k -> new ConcurrentHashMap<>())
                    .put(key(target), System.currentTimeMillis() + lifetime * 1000L);
            placed++;
        }

        if (placed > 0) {
            FX.playEffect(player.getLocation(), FX.versioned("GLASS_BREAK", "BLOCK_GLASS_BREAK", "BLOCK_GLASS_BREAK"));
            FX.playSound(player.getLocation(), FX.versioned("DIG_WOOL", "BLOCK_WOOL_PLACE", "BLOCK_WOOL_PLACE"));
        }
        return placed > 0;
    }

    /**
     * Resolve the bridge block material, preferring the team colour wool.
     */
    @SuppressWarnings("deprecation")
    private static Material bridgeMaterial(Player player, IArena arena, String group) {
        String configured = QStudioConfig.getString(group, "modern-items." + ModernItems.TEMPORARY_BRIDGE + ".block-material");
        if (configured != null && !configured.isEmpty()) {
            try {
                return Material.valueOf(configured.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        // default: team colour wool so the bridge matches the island
        var team = arena.getTeam(player);
        if (team != null) {
            try {
                return Material.valueOf(team.getColor().name() + "_WOOL");
            } catch (IllegalArgumentException ignored) {
            }
        }
        return Material.matchMaterial(QStudioConfig.getString(group, "modern-items." + ModernItems.TEMPORARY_BRIDGE + ".display-material")) == null
                ? Material.WOOL
                : Material.matchMaterial(QStudioConfig.getString(group, "modern-items." + ModernItems.TEMPORARY_BRIDGE + ".display-material"));
    }

    public static class BridgeListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onBreak(BlockBreakEvent e) {
            String world = e.getBlock().getWorld().getName();
            Map<String, Long> blocks = tempBlocks.get(world);
            if (blocks == null || !blocks.containsKey(key(e.getBlock()))) return;
            e.setCancelled(true);
            blocks.remove(key(e.getBlock()));
            e.getBlock().setType(Material.AIR);
            FX.playEffect(e.getBlock().getLocation(), FX.versioned("GLASS_BREAK", "BLOCK_GLASS_BREAK", "BLOCK_GLASS_BREAK"));
        }

        /**
         * Fade expired blocks. Runs once per second from the qStudio task.
         */
        public static void tick() {
            long now = System.currentTimeMillis();
            for (Map.Entry<String, Map<String, Long>> arenaEntry : tempBlocks.entrySet()) {
                Map<String, Long> blocks = arenaEntry.getValue();
                Iterator<Map.Entry<String, Long>> it = blocks.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Long> entry = it.next();
                    if (entry.getValue() > now) continue;
                    it.remove();
                    Block block = findBlock(arenaEntry.getKey(), entry.getKey());
                    if (block != null) {
                        Material previous = previousBlocks.get(arenaEntry.getKey()) == null ? null
                                : previousBlocks.get(arenaEntry.getKey()).get(entry.getKey());
                        block.setType(previous == null || previous == Material.AIR ? Material.AIR : previous);
                        FX.playEffect(block.getLocation(), FX.versioned("SMOKE", "SMOKE", "SMOKE"));
                    }
                }
            }
        }
    }

    /**
     * Remove temporary blocks owned by any team near a location (EMP support).
     */
    public static void removeBlocksNear(String world, org.bukkit.Location center, double radius) {
        Map<String, Long> blocks = tempBlocks.get(world);
        if (blocks == null) return;
        int radiusSquared = (int) (radius * radius);
        Iterator<Map.Entry<String, Long>> it = blocks.entrySet().iterator();
        while (it.hasNext()) {
            String key = it.next().getKey();
            Block block = findBlock(world, key);
            if (block == null) continue;
            if (block.getLocation().distanceSquared(center) > radiusSquared) continue;
            it.remove();
            block.setType(Material.AIR);
        }
    }

    public static void cleanupArena(String world) {
        Map<String, Long> blocks = tempBlocks.remove(world);
        previousBlocks.remove(world);
        if (blocks == null) return;
        for (String key : blocks.keySet()) {
            Block block = findBlock(world, key);
            if (block != null) block.setType(Material.AIR);
        }
    }

    private static String key(Block block) {
        return block.getX() + ";" + block.getY() + ";" + block.getZ();
    }

    private static Block findBlock(String world, String key) {
        org.bukkit.World bukkitWorld = org.bukkit.Bukkit.getWorld(world);
        if (bukkitWorld == null) return null;
        String[] parts = key.split(";");
        try {
            return bukkitWorld.getBlockAt(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (Exception ex) {
            return null;
        }
    }
}
