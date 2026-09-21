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

package com.andrei1058.bedwars.qstudio.config;

import com.andrei1058.bedwars.BedWars;
import org.bukkit.configuration.file.YamlConfiguration;

import static com.andrei1058.bedwars.BedWars.getForCurrentVersion;

/**
 * Default values of qstudio.yml.
 */
public final class YmlDefaults {

    private YmlDefaults() {
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void apply(YamlConfiguration yml) {
        yml.options().header("BedWars1058-qStudio configuration\n"
                + "By : qSa3ed\n"
                + "Every gameplay section can be overridden per arena group through profiles/*.yml");

        yml.addDefault("debug", false);
        yml.addDefault("active-profile", "default");
        yml.addDefault("profile-groups.Competitive", "competitive");

        /* Combat */
        yml.addDefault("combat.knockback-profiles.default.ground-horizontal", 1.0);
        yml.addDefault("combat.knockback-profiles.default.ground-vertical", 1.0);
        yml.addDefault("combat.knockback-profiles.default.air-horizontal", 1.15);
        yml.addDefault("combat.knockback-profiles.default.air-vertical", 1.05);
        yml.addDefault("combat.knockback-profiles.default.sprint-bonus", 0.2);
        yml.addDefault("combat.knockback-profiles.default.projectile-multiplier", 1.0);
        yml.addDefault("combat.knockback-profiles.default.explosion-multiplier", 1.0);
        yml.addDefault("combat.hit-feedback.enabled", true);
        yml.addDefault("combat.hit-feedback.action-bar", "&c-{damage} &7❤ &f{health}");
        yml.addDefault("combat.hit-feedback.show-particles", true);
        yml.addDefault("combat.combat-tag.enabled", true);
        yml.addDefault("combat.combat-tag.duration-seconds", 8);
        yml.addDefault("combat.combat-tag.block-ender-pearl", false);
        yml.addDefault("combat.combat-tag.action-bar", "&cCombat! &7{seconds}s");
        yml.addDefault("combat.projectiles.arrow.enabled", true);
        yml.addDefault("combat.projectiles.arrow.damage", 5.0);
        yml.addDefault("combat.projectiles.arrow.knockback-multiplier", 1.0);
        yml.addDefault("combat.projectiles.snowball.enabled", true);
        yml.addDefault("combat.projectiles.snowball.damage", 1.0);
        yml.addDefault("combat.projectiles.egg.enabled", true);
        yml.addDefault("combat.projectiles.egg.damage", 1.0);

        /* Explosives */
        yml.addDefault("explosives.fireball.explosion-radius", 3.0);
        yml.addDefault("explosives.fireball.damage", 6.0);
        yml.addDefault("explosives.fireball.knockback-multiplier", 1.0);
        yml.addDefault("explosives.fireball.vertical-boost", 0.4);
        yml.addDefault("explosives.fireball.block-damage-percent", 100);
        yml.addDefault("explosives.fireball.speed-multiplier", 1.0);
        yml.addDefault("explosives.tnt.explosion-power", 4.0);
        yml.addDefault("explosives.tnt.jump-enabled", true);
        yml.addDefault("explosives.tnt.jump-vertical", 0.9);
        yml.addDefault("explosives.tnt.jump-horizontal", 0.6);
        yml.addDefault("explosives.tnt.jump-damage-reduction", 0.5);

        /* Beds */
        yml.addDefault("beds.alarm.enabled", true);
        yml.addDefault("beds.alarm.radius", 6.0);
        yml.addDefault("beds.alarm.warn-interval-seconds", 10);
        yml.addDefault("beds.alarm.action-bar", "&c&l{player} &7is close to your bed!");
        yml.addDefault("beds.protection-phase.enabled", true);
        yml.addDefault("beds.protection-phase.duration-seconds", 30);
        yml.addDefault("beds.break-effects.particles", true);
        yml.addDefault("beds.break-effects.global-notification", true);

        /* Generators */
        yml.addDefault("generators.boost-events.enabled", true);
        yml.addDefault("generators.boost-events.interval-seconds", 300);
        yml.addDefault("generators.boost-events.duration-seconds", 60);
        yml.addDefault("generators.boost-events.multiplier", 2.0);
        yml.addDefault("generators.boost-events.announce", true);
        yml.addDefault("generators.pickup-range.forge-bonus-per-level", 0.5);

        /* Modern items compatibility layer */
        yml.addDefault("modern-items.mace.enabled", true);
        yml.addDefault("modern-items.mace.display-material", getForCurrentVersion("IRON_SPADE", "IRON_SHOVEL", "IRON_SHOVEL"));
        yml.addDefault("modern-items.mace.damage", 7.0);
        yml.addDefault("modern-items.mace.falling-multiplier", 1.5);
        yml.addDefault("modern-items.mace.smash-knockback", 1.2);
        yml.addDefault("modern-items.mace.smash-vertical", 0.6);
        yml.addDefault("modern-items.mace.cooldown-seconds", 5);
        yml.addDefault("modern-items.mace.particles", true);

        yml.addDefault("modern-items.wind-charge.enabled", true);
        yml.addDefault("modern-items.wind-charge.display-material", getForCurrentVersion("SNOW_BALL", "SNOW_BALL", "SNOWBALL"));
        yml.addDefault("modern-items.wind-charge.knockback", 1.1);
        yml.addDefault("modern-items.wind-charge.vertical-launch", 0.9);
        yml.addDefault("modern-items.wind-charge.horizontal-launch", 0.5);
        yml.addDefault("modern-items.wind-charge.damage", 1.0);
        yml.addDefault("modern-items.wind-charge.cooldown-seconds", 4);

        yml.addDefault("modern-items.firework-boost.enabled", true);
        yml.addDefault("modern-items.firework-boost.display-material", getForCurrentVersion("FIREWORK", "FIREWORK", "FIREWORK_ROCKET"));
        yml.addDefault("modern-items.firework-boost.strength", 1.6);
        yml.addDefault("modern-items.firework-boost.charges", 3);
        yml.addDefault("modern-items.firework-boost.cooldown-seconds", 2);

        yml.addDefault("modern-items.dash.enabled", true);
        yml.addDefault("modern-items.dash.display-material", "SUGAR");
        yml.addDefault("modern-items.dash.strength", 1.8);
        yml.addDefault("modern-items.dash.max-uses", 3);
        yml.addDefault("modern-items.dash.cooldown-seconds", 10);

        yml.addDefault("modern-items.grappling-hook.enabled", true);
        yml.addDefault("modern-items.grappling-hook.display-material", "FISHING_ROD");
        yml.addDefault("modern-items.grappling-hook.max-range", 40.0);
        yml.addDefault("modern-items.grappling-hook.pull-strength", 2.4);
        yml.addDefault("modern-items.grappling-hook.cooldown-seconds", 6);
        yml.addDefault("modern-items.grappling-hook.pull-enemies", false);

        yml.addDefault("modern-items.temporary-bridge.enabled", true);
        yml.addDefault("modern-items.temporary-bridge.display-material", getForCurrentVersion("WOOL", "WOOL", "WHITE_WOOL"));
        yml.addDefault("modern-items.temporary-bridge.lifetime-seconds", 20);
        yml.addDefault("modern-items.temporary-bridge.max-blocks", 64);
        yml.addDefault("modern-items.temporary-bridge.max-length", 12);
        yml.addDefault("modern-items.temporary-bridge.cooldown-seconds", 8);

        yml.addDefault("modern-items.bridge-builder.enabled", true);
        yml.addDefault("modern-items.bridge-builder.display-material", getForCurrentVersion("PISTON_BASE", "PISTON_BASE", "PISTON"));
        yml.addDefault("modern-items.bridge-builder.max-distance", 16);
        yml.addDefault("modern-items.bridge-builder.cooldown-seconds", 2);

        yml.addDefault("modern-items.teleport-beacon.enabled", true);
        yml.addDefault("modern-items.teleport-beacon.display-material", "BEACON");
        yml.addDefault("modern-items.teleport-beacon.activation-delay-seconds", 10);
        yml.addDefault("modern-items.teleport-beacon.teleport-cooldown-seconds", 30);
        yml.addDefault("modern-items.teleport-beacon.team-only", true);
        yml.addDefault("modern-items.teleport-beacon.lifetime-seconds", 300);
        yml.addDefault("modern-items.teleport-beacon.max-uses", 5);

        yml.addDefault("modern-items.emergency-teleport.enabled", true);
        yml.addDefault("modern-items.emergency-teleport.display-material", "ENDER_PEARL");
        yml.addDefault("modern-items.emergency-teleport.channel-seconds", 6);
        yml.addDefault("modern-items.emergency-teleport.cooldown-seconds", 60);
        yml.addDefault("modern-items.emergency-teleport.cancel-on-damage", true);
        yml.addDefault("modern-items.emergency-teleport.cancel-on-move", true);

        yml.addDefault("modern-items.base-shield.enabled", true);
        yml.addDefault("modern-items.base-shield.display-material", getForCurrentVersion("IRON_FENCE", "IRON_FENCE", "IRON_BARS"));
        yml.addDefault("modern-items.base-shield.duration-seconds", 15);
        yml.addDefault("modern-items.base-shield.damage-reduction", 0.5);
        yml.addDefault("modern-items.base-shield.projectile-reduction", 0.75);
        yml.addDefault("modern-items.base-shield.radius", 8.0);

        yml.addDefault("modern-items.emp-pulse.enabled", true);
        yml.addDefault("modern-items.emp-pulse.display-material", getForCurrentVersion("REDSTONE_TORCH_ON", "REDSTONE_TORCH_ON", "REDSTONE_TORCH"));
        yml.addDefault("modern-items.emp-pulse.radius", 10.0);
        yml.addDefault("modern-items.emp-pulse.cooldown-seconds", 15);

        /* Shop */
        yml.addDefault("shop.purchase-limits.enabled", true);
        yml.addDefault("shop.purchase-limits.default-per-life", -1);
        yml.addDefault("shop.purchase-limits.default-per-game", -1);
        yml.addDefault("shop.purchase-limits.cooldown-seconds", 0);
        yml.addDefault("shop.quick-buy.allow-reset", true);

        /* Progression */
        yml.addDefault("progression.quests.enabled", true);
        yml.addDefault("progression.quests.daily-reset-hours", 24);
        yml.addDefault("progression.quests.weekly-reset-days", 7);
        yml.addDefault("progression.achievements.enabled", true);
        yml.addDefault("progression.xp-per-bed-broken", 15);
        yml.addDefault("progression.xp-per-final-kill", 10);
        yml.addDefault("progression.xp-per-win", 50);

        /* example quest definitions - extendable in profiles */
        yml.addDefault("progression.quest-list.win-a-game.type", "wins");
        yml.addDefault("progression.quest-list.win-a-game.frequency", "daily");
        yml.addDefault("progression.quest-list.win-a-game.amount", 1);
        yml.addDefault("progression.quest-list.win-a-game.reward-xp", 100);
        yml.addDefault("progression.quest-list.win-a-game.reward-commands", java.util.Collections.singletonList("say {player} completed quest: Win a game!"));
        yml.addDefault("progression.quest-list.break-beds.type", "beds_broken");
        yml.addDefault("progression.quest-list.break-beds.frequency", "daily");
        yml.addDefault("progression.quest-list.break-beds.amount", 2);
        yml.addDefault("progression.quest-list.break-beds.reward-xp", 75);
        yml.addDefault("progression.quest-list.final-kills.type", "final_kills");
        yml.addDefault("progression.quest-list.final-kills.frequency", "weekly");
        yml.addDefault("progression.quest-list.final-kills.amount", 10);
        yml.addDefault("progression.quest-list.final-kills.reward-xp", 150);
        yml.addDefault("progression.quest-list.collect-resources.type", "resources_collected");
        yml.addDefault("progression.quest-list.collect-resources.frequency", "daily");
        yml.addDefault("progression.quest-list.collect-resources.amount", 250);
        yml.addDefault("progression.quest-list.collect-resources.reward-xp", 50);

        /* example achievements - one time goals with rewards */
        yml.addDefault("progression.achievement-list.first-blood.type", "kills");
        yml.addDefault("progression.achievement-list.first-blood.amount", 1);
        yml.addDefault("progression.achievement-list.first-blood.reward-xp", 25);
        yml.addDefault("progression.achievement-list.bed-breaker.type", "beds_broken");
        yml.addDefault("progression.achievement-list.bed-breaker.amount", 10);
        yml.addDefault("progression.achievement-list.bed-breaker.reward-xp", 100);
        yml.addDefault("progression.achievement-list.veteran.type", "games_played");
        yml.addDefault("progression.achievement-list.veteran.amount", 25);
        yml.addDefault("progression.achievement-list.veteran.reward-xp", 200);
    }
}
