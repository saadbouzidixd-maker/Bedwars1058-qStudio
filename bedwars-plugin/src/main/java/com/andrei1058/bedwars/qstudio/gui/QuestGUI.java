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

package com.andrei1058.bedwars.qstudio.gui;

import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.qstudio.progression.QuestDefinition;
import com.andrei1058.bedwars.qstudio.progression.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quest overview GUI listing every quest with its progress.
 */
public class QuestGUI implements Listener {

    public static final String TITLE = ChatColor.DARK_PURPLE + "qStudio Quests";
    private static final Map<UUID, Boolean> viewers = new ConcurrentHashMap<>();

    public static void open(Player player) {
        int size = Math.max(9, ((QuestManager.getQuests().size() / 9) + 1) * 9);
        Inventory inv = Bukkit.createInventory(null, size, TITLE);

        int slot = 0;
        for (QuestDefinition quest : QuestManager.getQuests().values()) {
            inv.setItem(slot++, buildIcon(player, quest));
        }

        player.openInventory(inv);
        viewers.put(player.getUniqueId(), true);
    }

    private static ItemStack buildIcon(Player player, QuestDefinition quest) {
        boolean completed = QuestManager.isCompleted(player.getUniqueId(), quest.getId());
        int progress = QuestManager.getProgress(player.getUniqueId(), quest.getId());

        Material material = completed ? Material.CHEST : Material.BOOK;
        ItemStack icon = new ItemStack(material, 1);
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) return icon;

        meta.setDisplayName(ChatColor.AQUA + quest.getId());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Type: " + ChatColor.WHITE + quest.getType());
        lore.add(ChatColor.GRAY + "Frequency: " + ChatColor.WHITE + quest.getFrequency());
        lore.add(ChatColor.GRAY + "Progress: " + ChatColor.YELLOW
                + Math.min(progress, quest.getAmount()) + "/" + quest.getAmount());
        if (quest.getRewardXp() > 0) {
            lore.add(ChatColor.GRAY + "Reward: " + ChatColor.GREEN + quest.getRewardXp() + " XP");
        }
        lore.add(completed ? ChatColor.GREEN + "Completed!" : ChatColor.YELLOW + "In progress");
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!TITLE.equals(e.getView().getTitle())) return;
        e.setCancelled(true);
    }

    /**
     * Drop the viewer marker when inventories close.
     */
    public static void onClose(UUID player) {
        viewers.remove(player);
    }
}
