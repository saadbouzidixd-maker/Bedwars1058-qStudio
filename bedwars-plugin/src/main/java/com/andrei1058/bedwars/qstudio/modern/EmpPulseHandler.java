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
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.qstudio.ModernItems;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * EMP pulse utility item.
 * <p>
 * Disables enemy temporary mechanics in a radius: team shields,
 * teleport beacons, emergency teleport channels and temporary bridge
 * blocks are purged when they belong to enemy teams.
 */
public class EmpPulseHandler implements ModernItemHandler {

    @Override
    public String id() {
        return ModernItems.EMP_PULSE;
    }

    @Override
    public boolean onUse(UseContext context) {
        Player player = context.getPlayer();
        IArena arena = context.getArena();
        String group = arena.getGroup();
        double radius = QStudioConfig.getDouble(group, "modern-items." + id() + ".radius");
        Location center = player.getLocation();

        int affected = 0;
        for (ITeam team : arena.getTeams()) {
            if (team.isMember(player)) continue;

            if (BaseShieldHandler.isShielded(arena.getWorldName(), team.getName())) {
                BaseShieldHandler.removeShield(arena.getWorldName(), team.getName());
                affected++;
            }
            for (Player member : team.getMembers()) {
                if (member.getLocation().distanceSquared(center) <= radius * radius) {
                    EmergencyTeleportHandler.forceCancel(member);
                    affected++;
                }
            }
        }

        TeleportBeaconHandler.disableNearby(arena.getWorldName(), center, radius);
        TemporaryBridgeHandler.removeBlocksNear(arena.getWorldName(), center, radius);

        FX.playEffect(center, FX.versioned("ENDER_SIGNAL", "ENDER_SIGNAL", "ENDER_SIGNAL"));
        FX.playSound(center, FX.versioned("NOTE_PLING", "BLOCK_NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING"));
        return affected > 0;
    }
}
