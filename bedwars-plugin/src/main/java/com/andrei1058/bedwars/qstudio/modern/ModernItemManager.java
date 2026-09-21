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
import com.andrei1058.bedwars.api.qstudio.ModernItems;
import com.andrei1058.bedwars.api.qstudio.events.QSItemUseEvent;
import com.andrei1058.bedwars.api.qstudio.QStudioApi;
import com.andrei1058.bedwars.api.server.VersionSupport;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.andrei1058.bedwars.BedWars.nms;

/**
 * Compatibility layer between modern gameplay mechanics and legacy versions.
 * <p>
 * Each modern item keeps a version specific display material (e.g. the mace
 * is an iron shovel on 1.8 servers) while behaviour is provided by handlers.
 * Items are identified through the version support NBT tag so no packet or
 * data component differences leak into the gameplay code.
 */
public class ModernItemManager implements Listener, QStudioApi {

    private static final String TAG_PREFIX = "qstudio:";
    private static final Map<String, ModernItemHandler> handlers = new HashMap<>();
    /** arena world -> player uuid -> item id -> available at (millis). */
    private static final Map<String, Map<UUID, Map<String, Long>>> cooldowns = new ConcurrentHashMap<>();

    private static ModernItemManager instance;

    public static ModernItemManager init() {
        if (instance != null) return instance;
        instance = new ModernItemManager();

        register(new MaceHandler());
        register(new WindChargeHandler());
        register(new FireworkBoostHandler());
        register(new DashHandler());
        register(new GrapplingHookHandler());
        register(new TemporaryBridgeHandler());
        register(new BridgeBuilderHandler());
        register(new TeleportBeaconHandler());
        register(new EmergencyTeleportHandler());
        register(new BaseShieldHandler());
        register(new EmpPulseHandler());

        // per version behaviour listeners that are not activated by clicks
        BedWars.registerEvents(
                new MaceHandler.HitListener(),
                new WindChargeHandler.ChargeListener(),
                new BaseShieldHandler.ShieldListener(),
                new EmergencyTeleportHandler.ChannelListener(),
                new TemporaryBridgeHandler.BridgeListener());

        BedWars.registerEvents(new GrapplingHookHandler());

        com.andrei1058.bedwars.api.qstudio.QStudio.register(instance);
        return instance;
    }

    public static void register(ModernItemHandler handler) {
        handlers.put(handler.id().toLowerCase(), handler);
    }

    /**
     * Tag an item stack as a modern item.
     */
    public static ItemStack tagItem(ItemStack item, String modernItemId) {
        return nms.setShopUpgradeIdentifier(item, TAG_PREFIX + modernItemId.toLowerCase());
    }

    /**
     * @return the raw version support tag of the stack, empty when absent.
     */
    public static String getRawTag(ItemStack stack) {
        if (stack == null || stack.getType() == org.bukkit.Material.AIR) return "";
        String identifier = nms.getShopUpgradeIdentifier(stack);
        return identifier == null ? "" : identifier;
    }

    /**
     * @return the modern item id of the stack or null.
     * Modern items may carry an extra payload after the id, separated by a colon.
     */
    public static String getItemId(ItemStack stack) {
        String raw = getRawTag(stack);
        if (raw.startsWith(TAG_PREFIX)) {
            String payload = raw.substring(TAG_PREFIX.length());
            int separator = payload.indexOf(':');
            return separator < 0 ? payload : payload.substring(0, separator);
        }
        return null;
    }

    public static boolean isEnabled(IArena arena, String id) {
        return QStudioConfig.getBoolean(arena.getGroup(), "modern-items." + id + ".enabled");
    }

    /**
     * Consume one piece of the held modern item and refresh the inventory.
     */
    public static void consumeOne(Player player, ItemStack item) {
        if (item.getAmount() <= 1) {
            player.getInventory().setItemInHand(null);
        } else {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItemInHand(item);
        }
        player.updateInventory();
    }

    /* cooldown handling */

    public static void setCooldown(Player player, IArena arena, String id, int seconds) {
        long until = System.currentTimeMillis() + seconds * 1000L;
        cooldowns
                .computeIfAbsent(arena.getWorldName(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(id, until);
    }

    public static boolean isOnCooldown(Player player, IArena arena, String id) {
        Map<UUID, Map<String, Long>> arenaMap = cooldowns.get(arena.getWorldName());
        if (arenaMap == null) return false;
        Map<String, Long> playerMap = arenaMap.get(player.getUniqueId());
        if (playerMap == null) return false;
        Long until = playerMap.get(id);
        return until != null && until > System.currentTimeMillis();
    }

    public static int remainingSeconds(Player player, IArena arena, String id) {
        Map<UUID, Map<String, Long>> arenaMap = cooldowns.get(arena.getWorldName());
        if (arenaMap == null) return 0;
        Map<String, Long> playerMap = arenaMap.get(player.getUniqueId());
        if (playerMap == null) return 0;
        Long until = playerMap.get(id);
        if (until == null) return 0;
        long remaining = until - System.currentTimeMillis();
        return remaining > 0 ? (int) Math.ceil(remaining / 1000.0) : 0;
    }

    /**
     * Remove every per arena cooldown entry.
     */
    public static void cleanupArena(String arenaWorld) {
        cooldowns.remove(arenaWorld);
        TemporaryBridgeHandler.cleanupArena(arenaWorld);
        TeleportBeaconHandler.cleanupArena(arenaWorld);
        BaseShieldHandler.cleanupArena(arenaWorld);
        EmergencyTeleportHandler.cleanupArena(arenaWorld);
        WindChargeHandler.ChargeListener.cleanup();
    }

    /**
     * Resolve the configured sound of a modern item with a version safe fallback.
     */
    public static String soundOf(IArena arena, String id, String fallback) {
        String configured = QStudioConfig.getString(arena.getGroup(), "modern-items." + id + ".sound");
        return configured == null || configured.isEmpty() ? fallback : configured;
    }

    /* interaction dispatch */

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null) return;

        String id = getItemId(e.getItem());
        if (id == null) return;

        Player player = e.getPlayer();
        IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(player);
        if (arena == null) return;
        if (arena.isSpectator(player) || arena.isReSpawning(player)) return;

        ModernItemHandler handler = handlers.get(id);
        if (handler == null) return;

        // prevent vanilla behaviour of the display item (pearl throw, rods, ...)
        e.setCancelled(true);

        if (!isEnabled(arena, id)) return;

        if (isOnCooldown(player, arena, id)) {
            int remaining = remainingSeconds(player, arena, id);
            nms.playAction(player, ChatColor.RED + handler.id() + " cooldown: " + remaining + "s");
            return;
        }

        QSItemUseEvent useEvent = new QSItemUseEvent(player, arena, id, e.getItem().clone());
        Bukkit.getPluginManager().callEvent(useEvent);
        if (useEvent.isCancelled()) return;

        int cooldown = QStudioConfig.getInt(arena.getGroup(), "modern-items." + id + ".cooldown-seconds");
        if (handler.onUse(new ModernItemHandler.UseContext(player, arena, e.getItem()))) {
            if (cooldown > 0) setCooldown(player, arena, id, cooldown);
        }
    }

    /* QStudioApi */

    @Override
    public ItemStack createModernItem(String type) {
        ModernItemHandler handler = handlers.get(type.toLowerCase());
        if (handler == null) return null;
        return null; // display items are built from config; use tagItem for behaviour
    }

    @Override
    public String getModernItemId(ItemStack stack) {
        return getItemId(stack);
    }

    @Override
    public int getCooldownSeconds(UUID player, String type) {
        for (Map<UUID, Map<String, Long>> arenaMap : cooldowns.values()) {
            Map<String, Long> playerMap = arenaMap.get(player);
            if (playerMap == null) continue;
            Long until = playerMap.get(type);
            if (until != null && until > System.currentTimeMillis()) {
                return (int) Math.ceil((until - System.currentTimeMillis()) / 1000.0);
            }
        }
        return 0;
    }

    @Override
    public int getQuestProgress(UUID player, String questId) {
        return com.andrei1058.bedwars.qstudio.progression.QuestManager.getProgress(player, questId);
    }

    @Override
    public java.util.List<String> getCompletedQuests(UUID player) {
        return com.andrei1058.bedwars.qstudio.progression.QuestManager.getCompletedQuestIds(player);
    }

    @Override
    public java.util.List<String> getUnlockedAchievements(UUID player) {
        return com.andrei1058.bedwars.qstudio.progression.AchievementManager.getUnlockedIds(player);
    }

    @Override
    public int getForgeLevel(com.andrei1058.bedwars.api.arena.team.ITeam team) {
        return com.andrei1058.bedwars.qstudio.upgrades.GeneratorBoostManager.getForgeLevel(team);
    }

    @Override
    public Map<String, java.util.List<String>> getGeneratorStatus(IArena arena) {
        return com.andrei1058.bedwars.qstudio.admin.GeneratorStatus.report(arena);
    }
}
