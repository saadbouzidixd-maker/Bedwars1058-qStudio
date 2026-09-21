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

        set(yml, "debug", false);
        set(yml, "active-profile", "default");
        set(yml, "profile-groups.Competitive", "competitive");

        /* Combat */
        set(yml, "combat.knockback-profiles.default.ground-horizontal", 1.0);
        set(yml, "combat.knockback-profiles.default.ground-vertical", 1.0);
        set(yml, "combat.knockback-profiles.default.air-horizontal", 1.15);
        set(yml, "combat.knockback-profiles.default.air-vertical", 1.05);
        set(yml, "combat.knockback-profiles.default.sprint-bonus", 0.2);
        set(yml, "combat.knockback-profiles.default.projectile-multiplier", 1.0);
        set(yml, "combat.knockback-profiles.default.explosion-multiplier", 1.0);
        set(yml, "combat.hit-feedback.enabled", true);
        set(yml, "combat.hit-feedback.action-bar", "&c-{damage} &7❤ &f{health}");
        set(yml, "combat.hit-feedback.show-particles", true);
        set(yml, "combat.combat-tag.enabled", true);
        set(yml, "combat.combat-tag.duration-seconds", 8);
        set(yml, "combat.combat-tag.block-ender-pearl", false);
        set(yml, "combat.combat-tag.action-bar", "&cCombat! &7{seconds}s");
        set(yml, "combat.projectiles.arrow.enabled", true);
        set(yml, "combat.projectiles.arrow.damage", 5.0);
        set(yml, "combat.projectiles.arrow.knockback-multiplier", 1.0);
        set(yml, "combat.projectiles.snowball.enabled", true);
        set(yml, "combat.projectiles.snowball.damage", 1.0);
        set(yml, "combat.projectiles.egg.enabled", true);
        set(yml, "combat.projectiles.egg.damage", 1.0);

        /* Explosives */
        set(yml, "explosives.fireball.explosion-radius", 3.0);
        set(yml, "explosives.fireball.damage", 6.0);
        set(yml, "explosives.fireball.knockback-multiplier", 1.0);
        set(yml, "explosives.fireball.vertical-boost", 0.4);
        set(yml, "explosives.fireball.block-damage-percent", 100);
        set(yml, "explosives.fireball.speed-multiplier", 1.0);
        set(yml, "explosives.tnt.explosion-power", 4.0);
        set(yml, "explosives.tnt.jump-enabled", true);
        set(yml, "explosives.tnt.jump-vertical", 0.9);
        set(yml, "explosives.tnt.jump-horizontal", 0.6);
        set(yml, "explosives.tnt.jump-damage-reduction", 0.5);

        /* Beds */
        set(yml, "beds.alarm.enabled", true);
        set(yml, "beds.alarm.radius", 6.0);
        set(yml, "beds.alarm.warn-interval-seconds", 10);
        set(yml, "beds.alarm.action-bar", "&c&l{player} &7is close to your bed!");
        set(yml, "beds.protection-phase.enabled", true);
        set(yml, "beds.protection-phase.duration-seconds", 30);
        set(yml, "beds.break-effects.particles", true);
        set(yml, "beds.break-effects.global-notification", true);

        /* Generators */
        set(yml, "generators.boost-events.enabled", true);
        set(yml, "generators.boost-events.interval-seconds", 300);
        set(yml, "generators.boost-events.duration-seconds", 60);
        set(yml, "generators.boost-events.multiplier", 2.0);
        set(yml, "generators.boost-events.announce", true);
        set(yml, "generators.pickup-range.forge-bonus-per-level", 0.5);

        /* Modern items compatibility layer */
        set(yml, "modern-items.mace.enabled", true);
        set(yml, "modern-items.mace.display-material", getForCurrentVersion("IRON_SPADE", "IRON_SHOVEL", "IRON_SHOVEL"));
        set(yml, "modern-items.mace.damage", 7.0);
        set(yml, "modern-items.mace.falling-multiplier", 1.5);
        set(yml, "modern-items.mace.smash-knockback", 1.2);
        set(yml, "modern-items.mace.smash-vertical", 0.6);
        set(yml, "modern-items.mace.cooldown-seconds", 5);
        set(yml, "modern-items.mace.particles", true);

        set(yml, "modern-items.wind-charge.enabled", true);
        set(yml, "modern-items.wind-charge.display-material", getForCurrentVersion("SNOW_BALL", "SNOW_BALL", "SNOWBALL"));
        set(yml, "modern-items.wind-charge.knockback", 1.1);
        set(yml, "modern-items.wind-charge.vertical-launch", 0.9);
        set(yml, "modern-items.wind-charge.horizontal-launch", 0.5);
        set(yml, "modern-items.wind-charge.damage", 1.0);
        set(yml, "modern-items.wind-charge.cooldown-seconds", 4);

        set(yml, "modern-items.firework-boost.enabled", true);
        set(yml, "modern-items.firework-boost.display-material", getForCurrentVersion("FIREWORK", "FIREWORK", "FIREWORK_ROCKET"));
        set(yml, "modern-items.firework-boost.strength", 1.6);
        set(yml, "modern-items.firework-boost.charges", 3);
        set(yml, "modern-items.firework-boost.cooldown-seconds", 2);

        set(yml, "modern-items.dash.enabled", true);
        set(yml, "modern-items.dash.display-material", "SUGAR");
        set(yml, "modern-items.dash.strength", 1.8);
        set(yml, "modern-items.dash.max-uses", 3);
        set(yml, "modern-items.dash.cooldown-seconds", 10);

        set(yml, "modern-items.grappling-hook.enabled", true);
        set(yml, "modern-items.grappling-hook.display-material", "FISHING_ROD");
        set(yml, "modern-items.grappling-hook.max-range", 40.0);
        set(yml, "modern-items.grappling-hook.pull-strength", 2.4);
        set(yml, "modern-items.grappling-hook.cooldown-seconds", 6);
        set(yml, "modern-items.grappling-hook.pull-enemies", false);

        set(yml, "modern-items.temporary-bridge.enabled", true);
        set(yml, "modern-items.temporary-bridge.display-material", getForCurrentVersion("WOOL", "WOOL", "WHITE_WOOL"));
        set(yml, "modern-items.temporary-bridge.lifetime-seconds", 20);
        set(yml, "modern-items.temporary-bridge.max-blocks", 64);
        set(yml, "modern-items.temporary-bridge.max-length", 12);
        set(yml, "modern-items.temporary-bridge.cooldown-seconds", 8);

        set(yml, "modern-items.bridge-builder.enabled", true);
        set(yml, "modern-items.bridge-builder.display-material", getForCurrentVersion("PISTON_BASE", "PISTON_BASE", "PISTON"));
        set(yml, "modern-items.bridge-builder.max-distance", 16);
        set(yml, "modern-items.bridge-builder.cooldown-seconds", 2);

        set(yml, "modern-items.teleport-beacon.enabled", true);
        set(yml, "modern-items.teleport-beacon.display-material", "BEACON");
        set(yml, "modern-items.teleport-beacon.activation-delay-seconds", 10);
        set(yml, "modern-items.teleport-beacon.teleport-cooldown-seconds", 30);
        set(yml, "modern-items.teleport-beacon.team-only", true);
        set(yml, "modern-items.teleport-beacon.lifetime-seconds", 300);
        set(yml, "modern-items.teleport-beacon.max-uses", 5);

        set(yml, "modern-items.emergency-teleport.enabled", true);
        set(yml, "modern-items.emergency-teleport.display-material", "ENDER_PEARL");
        set(yml, "modern-items.emergency-teleport.channel-seconds", 6);
        set(yml, "modern-items.emergency-teleport.cooldown-seconds", 60);
        set(yml, "modern-items.emergency-teleport.cancel-on-damage", true);
        set(yml, "modern-items.emergency-teleport.cancel-on-move", true);

        set(yml, "modern-items.base-shield.enabled", true);
        set(yml, "modern-items.base-shield.display-material", getForCurrentVersion("IRON_FENCE", "IRON_FENCE", "IRON_BARS"));
        set(yml, "modern-items.base-shield.duration-seconds", 15);
        set(yml, "modern-items.base-shield.damage-reduction", 0.5);
        set(yml, "modern-items.base-shield.projectile-reduction", 0.75);
        set(yml, "modern-items.base-shield.radius", 8.0);

        set(yml, "modern-items.emp-pulse.enabled", true);
        set(yml, "modern-items.emp-pulse.display-material", getForCurrentVersion("REDSTONE_TORCH_ON", "REDSTONE_TORCH_ON", "REDSTONE_TORCH"));
        set(yml, "modern-items.emp-pulse.radius", 10.0);
        set(yml, "modern-items.emp-pulse.cooldown-seconds", 15);

        /* Shop */
        set(yml, "shop.purchase-limits.enabled", true);
        set(yml, "shop.purchase-limits.default-per-life", -1);
        set(yml, "shop.purchase-limits.default-per-game", -1);
        set(yml, "shop.purchase-limits.cooldown-seconds", 0);
        set(yml, "shop.quick-buy.allow-reset", true);

        /* Progression */
        set(yml, "progression.quests.enabled", true);
        set(yml, "progression.quests.daily-reset-hours", 24);
        set(yml, "progression.quests.weekly-reset-days", 7);
        set(yml, "progression.achievements.enabled", true);
        set(yml, "progression.xp-per-bed-broken", 15);
        set(yml, "progression.xp-per-final-kill", 10);
        set(yml, "progression.xp-per-win", 50);

        /* example quest definitions - extendable in profiles */
        set(yml, "progression.quest-list.win-a-game.type", "wins");
        set(yml, "progression.quest-list.win-a-game.frequency", "daily");
        set(yml, "progression.quest-list.win-a-game.amount", 1);
        set(yml, "progression.quest-list.win-a-game.reward-xp", 100);
        set(yml, "progression.quest-list.win-a-game.reward-commands", java.util.Collections.singletonList("say {player} completed quest: Win a game!"));
        set(yml, "progression.quest-list.break-beds.type", "beds_broken");
        set(yml, "progression.quest-list.break-beds.frequency", "daily");
        set(yml, "progression.quest-list.break-beds.amount", 2);
        set(yml, "progression.quest-list.break-beds.reward-xp", 75);
        set(yml, "progression.quest-list.final-kills.type", "final_kills");
        set(yml, "progression.quest-list.final-kills.frequency", "weekly");
        set(yml, "progression.quest-list.final-kills.amount", 10);
        set(yml, "progression.quest-list.final-kills.reward-xp", 150);
        set(yml, "progression.quest-list.collect-resources.type", "resources_collected");
        set(yml, "progression.quest-list.collect-resources.frequency", "daily");
        set(yml, "progression.quest-list.collect-resources.amount", 250);
        set(yml, "progression.quest-list.collect-resources.reward-xp", 50);

        /* example achievements - one time goals with rewards */
        set(yml, "progression.achievement-list.first-blood.type", "kills");
        set(yml, "progression.achievement-list.first-blood.amount", 1);
        set(yml, "progression.achievement-list.first-blood.reward-xp", 25);
        set(yml, "progression.achievement-list.bed-breaker.type", "beds_broken");
        set(yml, "progression.achievement-list.bed-breaker.amount", 10);
        set(yml, "progression.achievement-list.bed-breaker.reward-xp", 100);
        set(yml, "progression.achievement-list.veteran.type", "games_played");
        set(yml, "progression.achievement-list.veteran.amount", 25);
        set(yml, "progression.achievement-list.veteran.reward-xp", 200);
    }

    /**
     * Set a value on the configuration only when absent, so user edits
     * survive regeneration of missing defaults.
     */
    private static void set(YamlConfiguration yml, String path, Object value) {
        if (!yml.isSet(path)) {
            yml.set(path, value);
        }
    }
}
