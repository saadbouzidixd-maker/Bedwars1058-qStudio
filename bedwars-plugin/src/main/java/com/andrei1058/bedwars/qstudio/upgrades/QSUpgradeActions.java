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
import com.andrei1058.bedwars.api.upgrades.UpgradeAction;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Team upgrade actions added by qStudio.
 * <p>
 * Selected through the receive list of an upgrade tier in upgrades2:
 * forge, trap-capacity, regen-aura, defense-aura, anti-invisibility,
 * gen-boost.
 */
public final class QSUpgradeActions {

    private QSUpgradeActions() {
    }

    /**
     * Increase the generator output of the team. The tier value defines
     * the forge level.
     */
    public static class ForgeAction implements UpgradeAction {

        public String getName() {
            return "forge";
        }

        @Override
        public void onBuy(Player player, ITeam team) {
            int current = GeneratorBoostManager.getForgeLevel(team);
            GeneratorBoostManager.setForgeLevel(team, current + 1);
            team.getArena().getTeams().forEach(t -> t.getMembers().forEach(member ->
                    member.sendMessage(org.bukkit.ChatColor.GOLD + com.andrei1058.bedwars.api.language.Language.getMsg(member,
                            "qstudio.forge-upgraded").replace("{level}", String.valueOf(current + 1)))));
        }
    }

    /**
     * Increase the amount of traps a team can queue.
     */
    public static class TrapCapacityAction implements UpgradeAction {

        public String getName() {
            return "trap-capacity";
        }

        @Override
        public void onBuy(Player player, ITeam team) {
            AuraRegistry.addCapacity(team, 1);
        }
    }

    /**
     * Give regeneration to team members while they are on their island.
     */
    public static class RegenAuraAction implements UpgradeAction {

        private final int amplifier;

        public RegenAuraAction(int amplifier) {
            this.amplifier = Math.max(0, amplifier);
        }

        public String getName() {
            return "regen-aura";
        }

        @Override
        public void onBuy(Player player, ITeam team) {
            AuraRegistry.enableRegenAura(team, amplifier);
        }
    }

    /**
     * Weaken enemies that enter the island: slowness on intruders.
     */
    public static class DefenseAuraAction implements UpgradeAction {

        private final int amplifier;

        public DefenseAuraAction(int amplifier) {
            this.amplifier = Math.max(0, amplifier);
        }

        public String getName() {
            return "defense-aura";
        }

        @Override
        public void onBuy(Player player, ITeam team) {
            AuraRegistry.enableDefenseAura(team, amplifier);
        }
    }

    /**
     * Reveal invisible enemies near the team base.
     */
    public static class AntiInvisibilityAction implements UpgradeAction {

        public String getName() {
            return "anti-invisibility";
        }

        @Override
        public void onBuy(Player player, ITeam team) {
            AuraRegistry.enableRevealAura(team);
        }
    }

    /**
     * Temporary double speed generator boost for the team.
     */
    public static class GenBoostAction implements UpgradeAction {

        private final int seconds;

        public GenBoostAction(int seconds) {
            this.seconds = seconds;
        }

        public String getName() {
            return "gen-boost";
        }

        @Override
        public void onBuy(Player player, ITeam team) {
            GeneratorBoostManager.setBoost(team, seconds);
        }
    }
}
