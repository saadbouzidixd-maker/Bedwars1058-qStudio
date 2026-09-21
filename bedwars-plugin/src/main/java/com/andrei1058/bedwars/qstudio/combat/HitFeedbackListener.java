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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static com.andrei1058.bedwars.BedWars.nms;

/**
 * Hit feedback for attackers: damage indicators on the action bar and
 * optional crit particles on the victim.
 */
public class HitFeedbackListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) return;
        Player attacker = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();
        IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(attacker);
        if (arena == null || arena != com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(victim)) return;

        String group = arena.getGroup();
        if (!QStudioConfig.getBoolean(group, "combat.hit-feedback.enabled")) return;

        double health = Math.max(0, victim.getHealth() - e.getFinalDamage());
        String template = QStudioConfig.getString(group, "combat.hit-feedback.action-bar");
        if (template != null && !template.isEmpty()) {
            String text = template
                    .replace("{damage}", String.format("%.1f", e.getFinalDamage()))
                    .replace("{health}", String.format("%.1f", health));
            nms.playAction(attacker, org.bukkit.ChatColor.translateAlternateColorCodes('&', text));
        }

        if (QStudioConfig.getBoolean(group, "combat.hit-feedback.show-particles")) {
            com.andrei1058.bedwars.qstudio.modern.FX.playEffect(victim.getLocation(),
                    com.andrei1058.bedwars.qstudio.modern.FX.versioned("CRIT", "CRIT", "CRIT"));
        }
    }
}
