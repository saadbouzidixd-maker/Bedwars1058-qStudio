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

package com.andrei1058.bedwars.qstudio.upgrades;

import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.upgrades.TrapAction;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Trap actions added by qStudio.
 * <p>
 * They are selected through the receive list of a base trap in
 * upgrades2 with the types mark-enemy, knockback-enemy, alarm,
 * reveal-invisible and exhaust-enemy.
 */
public final class QSTrapActions {

    private QSTrapActions() {
    }

    /**
     * Mark an enemy: glowing outline is not available on legacy versions,
     * so the enemy is surrounded by particles visible for the defenders
     * and receives a name tag suffix on their display name for a while.
     */
    public static class MarkEnemyAction implements TrapAction {

        private final int seconds;

        public MarkEnemyAction(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public String getName() {
            return "mark-enemy";
        }

        @Override
        public void onTrigger(Player player, ITeam playerTeam, ITeam targetTeam) {
            for (Player member : targetTeam.getMembers()) {
                member.sendMessage(ChatColor.RED + player.getName() + " is marked!");
            }
            com.andrei1058.bedwars.qstudio.modern.FX.playEffect(player.getLocation(),
                    com.andrei1058.bedwars.qstudio.modern.FX.versioned("MOBSPAWNER_FLAMES", "MOBSPAWNER_FLAMES", "MOBSPAWNER_FLAMES"));
            player.setDisplayName(ChatColor.RED + "[Marked] " + player.getName() + ChatColor.RESET);
            long expiry = System.currentTimeMillis() + seconds * 1000L;
            MarkingTracker.mark(player.getUniqueId(), expiry, player.getDisplayName());
        }
    }

    /**
     * Push the enemy away from the team base.
     */
    public static class KnockbackEnemyAction implements TrapAction {

        private final double strength;
        private final double vertical;

        public KnockbackEnemyAction(double strength, double vertical) {
            this.strength = strength;
            this.vertical = vertical;
        }

        @Override
        public String getName() {
            return "knockback-enemy";
        }

        @Override
        public void onTrigger(Player player, ITeam playerTeam, ITeam targetTeam) {
            Location bed = targetTeam.getBed();
            if (bed == null) return;
            Vector away = player.getLocation().toVector().subtract(bed.toVector());
            away.setY(0);
            if (away.lengthSquared() < 0.01) {
                away = new Vector(0, 0, 1);
            } else {
                away.normalize();
            }
            player.setVelocity(away.multiply(strength).setY(vertical));
        }
    }

    /**
     * Notify the team with an action bar and sound, no effects on the enemy.
     */
    public static class AlarmAction implements TrapAction {

        @Override
        public String getName() {
            return "alarm";
        }

        @Override
        public void onTrigger(Player player, ITeam playerTeam, ITeam targetTeam) {
            for (Player member : targetTeam.getMembers()) {
                com.andrei1058.bedwars.BedWars.nms.playAction(member,
                        com.andrei1058.bedwars.api.language.Language.getMsg(member,
                                "qstudio.trap-alarm").replace("{player}", player.getName()));
            }
        }
    }

    /**
     * Remove invisibility potions from the enemy.
     */
    public static class RevealInvisibleAction implements TrapAction {

        @Override
        public String getName() {
            return "reveal-invisible";
        }

        @Override
        public void onTrigger(Player player, ITeam playerTeam, ITeam targetTeam) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            for (Player member : targetTeam.getMembers()) {
                member.sendMessage(ChatColor.YELLOW + player.getName() + " lost their invisibility!");
            }
        }
    }

    /**
     * Movement and combat penalties: slow digging, weakness and hunger.
     */
    public static class ExhaustEnemyAction implements TrapAction {

        private final int duration;

        public ExhaustEnemyAction(int duration) {
            this.duration = duration;
        }

        @Override
        public String getName() {
            return "exhaust-enemy";
        }

        @Override
        public void onTrigger(Player player, ITeam playerTeam, ITeam targetTeam) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, duration * 20, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration * 20, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, duration * 20, 1));
        }
    }
}
