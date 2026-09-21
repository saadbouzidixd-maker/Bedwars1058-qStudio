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

package com.andrei1058.bedwars.qstudio;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.events.server.ArenaDisableEvent;
import com.andrei1058.bedwars.qstudio.beds.BedDefenseListener;
import com.andrei1058.bedwars.qstudio.combat.CombatTagListener;
import com.andrei1058.bedwars.qstudio.combat.ExplosivesListener;
import com.andrei1058.bedwars.qstudio.combat.HitFeedbackListener;
import com.andrei1058.bedwars.qstudio.combat.KnockbackListener;
import com.andrei1058.bedwars.qstudio.combat.ProjectileEnhancer;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import com.andrei1058.bedwars.qstudio.gui.QuestsCommand;
import com.andrei1058.bedwars.qstudio.modern.ModernItemManager;
import com.andrei1058.bedwars.qstudio.progression.AchievementManager;
import com.andrei1058.bedwars.qstudio.progression.QuestListener;
import com.andrei1058.bedwars.qstudio.progression.QuestManager;
import com.andrei1058.bedwars.qstudio.qmessages.QMessages;
import com.andrei1058.bedwars.qstudio.shop.PurchaseLimitListener;
import com.andrei1058.bedwars.qstudio.admin.QStudioAdminCommand;
import com.andrei1058.bedwars.qstudio.upgrades.AuraRegistry;
import com.andrei1058.bedwars.qstudio.upgrades.GeneratorBoostManager;
import com.andrei1058.bedwars.qstudio.upgrades.MarkingTracker;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Bootstrap of the qStudio layer.
 * Loads the configuration, registers the systems and owns the one
 * second heartbeat used by periodic mechanics.
 */
public final class QStudio implements Listener {

    private static boolean initialized = false;

    private QStudio() {
    }

    /**
     * Initialize the whole qStudio layer. Safe to call once per enable.
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        QStudioConfig.init();
        QMessages.registerDefaults();
        ModernItemManager.init();

        BedWars.registerEvents(
                new QStudio(),
                new KnockbackListener(),
                new HitFeedbackListener(),
                new CombatTagListener(),
                new ProjectileEnhancer(),
                new ExplosivesListener(),
                new BedDefenseListener(),
                new QuestListener(),
                new PurchaseLimitListener());

        QuestManager.init();
        AchievementManager.init();

        // heartbeat
        Bukkit.getScheduler().runTaskTimer(BedWars.plugin, QStudio::tick, 20L, 20L);

        // commands
        com.andrei1058.bedwars.commands.bedwars.MainCommand mainCommand =
                com.andrei1058.bedwars.commands.bedwars.MainCommand.getInstance();
        if (mainCommand != null) {
            new QStudioAdminCommand(mainCommand, "qstudio");
            new QuestsCommand(mainCommand, "quests");
        }
    }

    /**
     * One second heartbeat for every periodic mechanic.
     */
    private static void tick() {
        com.andrei1058.bedwars.qstudio.modern.TemporaryBridgeHandler.BridgeListener.tick();
        com.andrei1058.bedwars.qstudio.modern.EmergencyTeleportHandler.ChannelListener.tick();
        com.andrei1058.bedwars.qstudio.modern.BaseShieldHandler.tick();
        com.andrei1058.bedwars.qstudio.modern.WindChargeHandler.ChargeListener.cleanup();
        AuraRegistry.tick();
        GeneratorBoostManager.tick();
        GeneratorBoostManager.tickBoostEvents();
        BedDefenseListener.tickProtection();
        BedDefenseListener.tickAlarm();
        MarkingTracker.tick();
        CombatTagListener.tick();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        QuestManager.unloadPlayer(e.getPlayer().getUniqueId());
        AchievementManager.unloadPlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onArenaDisable(ArenaDisableEvent e) {
        ModernItemManager.cleanupArena(e.getWorldName());
        GeneratorBoostManager.cleanupArena(e.getWorldName());
        AuraRegistry.cleanupArena(e.getWorldName());
        BedDefenseListener.cleanupArena(e.getWorldName());
    }
}
