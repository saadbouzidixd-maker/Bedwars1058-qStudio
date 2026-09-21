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

import com.andrei1058.bedwars.api.qstudio.ModernItems;
import com.andrei1058.bedwars.qstudio.config.QStudioConfig;

/**
 * Firework boost movement item.
 * <p>
 * Activating it launches the player towards the direction they are
 * looking. Each item has a limited amount of charges; consuming the last
 * one removes the item.
 */
public class FireworkBoostHandler implements ModernItemHandler {

    @Override
    public String id() {
        return ModernItems.FIREWORK_BOOST;
    }

    @Override
    public boolean onUse(UseContext context) {
        var player = context.getPlayer();
        var group = context.getArena().getGroup();
        double strength = QStudioConfig.getDouble(group, "modern-items." + id() + ".strength");

        var direction = player.getLocation().getDirection().normalize();
        direction.setY(Math.max(direction.getY(), 0.25));
        player.setVelocity(direction.multiply(strength));

        FX.playEffect(player.getLocation(), FX.versioned("FIREWORKS_SPARK", "FIREWORKS_SPARK", "FIREWORKS_SPARK"));
        FX.playSound(player.getLocation(), FX.versioned("FIREWORK_LAUNCH", "ENTITY_FIREWORK_LAUNCH", "ENTITY_FIREWORK_ROCKET_LAUNCH"));

        ModernItemManager.consumeOne(player, context.getItem());
        return false;
    }
}
