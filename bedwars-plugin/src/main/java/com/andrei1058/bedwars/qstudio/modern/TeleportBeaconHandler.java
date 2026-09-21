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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Temporary teleport beacon.
 * <p>
 * Places a beacon block near the player. After an activation delay the
 * team members can use the item again to warp to the beacon. Enemies
 * standing close during the activation interrupt it. Beacons expire
 * after a lifetime and have a limited amount of uses.
 */
public class TeleportBeaconHandler implements ModernItemHandler {

    /** world -> team name -> beacon. */
    private static final Map<String, Map<String, Beacon>> beacons = new LinkedHashMap<>();

    @Override
    public String id() {
        return ModernItems.TELEPORT_BEACON;
    }

    @Override
    public boolean onUse(UseContext context) {
        Player player = context.getPlayer();
        ITeam team = context.getArena().getTeam(player);
        if (team == null) return false;

        Beacon existing = getBeacon(context.getArena().getWorldName(), team.getName());
        if (existing == null) {
            return placeBeacon(context, team);
        }

        // beacon exists: try to warp
        if (!existing.isActivated()) {
            BedWars.plugin.nms.playAction(player, com.andrei1058.bedwars.api.language.Language.getMsg(player,
                    "qstudio.beacon-not-ready"));
            return false;
        }
        if (existing.getUses() <= 0) {
            return false;
        }
        existing.useOnce();
        player.teleport(existing.getLocation().add(0.5, 1.2, 0.5));
        FX.playSound(player.getLocation(), FX.versioned("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT"));
        FX.playEffect(existing.getLocation(), FX.versioned("PORTAL", "PORTAL", "PORTAL"));
        if (existing.getUses() <= 0) {
            removeBeacon(context.getArena().getWorldName(), team.getName());
        }
        return false;
    }

    private boolean placeBeacon(UseContext context, ITeam team) {
        Player player = context.getPlayer();
        IArena arena = context.getArena();
        String group = arena.getGroup();

        int activationDelay = QStudioConfig.getInt(group, "modern-items." + id() + ".activation-delay-seconds");
        int lifetime = QStudioConfig.getInt(group, "modern-items." + id() + ".lifetime-seconds");
        int maxUses = QStudioConfig.getInt(group, "modern-items." + id() + ".max-uses");
        boolean teamOnly = QStudioConfig.getBoolean(group, "modern-items." + id() + ".team-only");

        @SuppressWarnings("deprecation")
        Block target = player.getTargetBlock((java.util.Set<org.bukkit.Material>) null, 6);
        if (target == null || target.getType() != Material.AIR) {
            return false;
        }

        target.setType(Material.BEACON);
        Beacon beacon = new Beacon(team.getName(), target.getLocation(),
                System.currentTimeMillis() + activationDelay * 1000L,
                System.currentTimeMillis() + lifetime * 1000L, maxUses, teamOnly);
        beacons.computeIfAbsent(arena.getWorldName(), k -> new LinkedHashMap<>()).put(team.getName(), beacon);

        // activation watchdog: enemy proximity cancels the beacon
        final String world = arena.getWorldName();
        beacon.setTask(Bukkit.getScheduler().runTaskTimer(BedWars.plugin, () -> {
            Beacon current = getBeacon(world, team.getName());
            if (current == null) {
                return;
            }
            long now = System.currentTimeMillis();
            if (now >= current.getExpiresAt()) {
                removeBeacon(world, team.getName());
                return;
            }
            if (!current.isActivated()) {
                for (Player enemy : arena.getPlayers()) {
                    if (team.isMember(enemy)) continue;
                    if (enemy.getLocation().distance(current.getLocation()) <= 4) {
                        removeBeacon(world, team.getName());
                        for (Player member : team.getMembers()) {
                            BedWars.plugin.nms.playAction(member, com.andrei1058.bedwars.api.language.Language.getMsg(member,
                                    "qstudio.beacon-interrupted"));
                        }
                        return;
                    }
                }
            }
        }, 20L, 20L));

        FX.playSound(target.getLocation(), FX.versioned("NOTE_PLING", "BLOCK_NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING"));
        return false;
    }

    private static Beacon getBeacon(String world, String team) {
        Map<String, Beacon> map = beacons.get(world);
        return map == null ? null : map.get(team);
    }

    private static void removeBeacon(String world, String team) {
        Map<String, Beacon> map = beacons.get(world);
        if (map == null) return;
        Beacon beacon = map.remove(team);
        if (beacon != null) {
            beacon.destroy();
        }
    }

    /**
     * @return true when the block is an active qStudio beacon.
     */
    public static boolean isBeaconBlock(Block block) {
        Map<String, Beacon> map = beacons.get(block.getWorld().getName());
        if (map == null) return false;
        for (Beacon beacon : map.values()) {
            if (beacon.sameBlock(block.getLocation())) return true;
        }
        return false;
    }

    /**
     * Disable enemy beacons near a location (EMP support).
     */
    public static void disableNearby(String world, Location center, double radius) {
        Map<String, Beacon> map = beacons.get(world);
        if (map == null) return;
        Iterator<Map.Entry<String, Beacon>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Beacon beacon = it.next().getValue();
            if (beacon.getLocation().distanceSquared(center) <= radius * radius) {
                it.remove();
                beacon.destroy();
            }
        }
    }

    public static void cleanupArena(String world) {
        Map<String, Beacon> map = beacons.remove(world);
        if (map == null) return;
        for (Beacon beacon : map.values()) {
            beacon.destroy();
        }
    }

    private static class Beacon {
        private final String team;
        private final Location location;
        private final long activatesAt;
        private final long expiresAt;
        private int uses;
        private final boolean teamOnly;
        private BukkitTask task;

        Beacon(String team, Location location, long activatesAt, long expiresAt, int uses, boolean teamOnly) {
            this.team = team;
            this.location = location;
            this.activatesAt = activatesAt;
            this.expiresAt = expiresAt;
            this.uses = uses;
            this.teamOnly = teamOnly;
        }

        void setTask(BukkitTask task) {
            this.task = task;
        }

        boolean isActivated() {
            return System.currentTimeMillis() >= activatesAt;
        }

        long getExpiresAt() {
            return expiresAt;
        }

        int getUses() {
            return uses;
        }

        void useOnce() {
            uses--;
        }

        boolean isTeamOnly() {
            return teamOnly;
        }

        Location getLocation() {
            return location;
        }

        String getTeam() {
            return team;
        }

        boolean sameBlock(Location other) {
            return location.getWorld().equals(other.getWorld())
                    && location.getBlockX() == other.getBlockX()
                    && location.getBlockY() == other.getBlockY()
                    && location.getBlockZ() == other.getBlockZ();
        }

        void destroy() {
            if (task != null) {
                task.cancel();
            }
            Block block = location.getBlock();
            if (block.getType() == Material.BEACON) {
                block.setType(Material.AIR);
            }
        }
    }
}
