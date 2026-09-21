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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Behaviour of a modern item on legacy servers.
 * <p>
 * A handler only owns the activation logic. Identification of the display
 * item is handled by {@link ModernItemManager} through the version support
 * NBT tag, so the same code path works from 1.8 up to the latest release.
 */
public interface ModernItemHandler {

    /**
     * @return modern item identifier, see {@link com.andrei1058.bedwars.api.qstudio.ModernItems}.
     */
    String id();

    /**
     * Called when the player activates the item.
     *
     * @return true when the activation consumed the item or a use.
     */
    boolean onUse(UseContext context);

    /**
     * Activation context passed to handlers.
     */
    class UseContext {
        private final Player player;
        private final IArena arena;
        private final ItemStack item;

        public UseContext(Player player, IArena arena, ItemStack item) {
            this.player = player;
            this.arena = arena;
            this.item = item;
        }

        public Player getPlayer() {
            return player;
        }

        public IArena getArena() {
            return arena;
        }

        public ItemStack getItem() {
            return item;
        }
    }
}
