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

package com.andrei1058.bedwars.api.qstudio.events;

import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a base trap is about to trigger for an enemy player.
 * Cancelling prevents the trap from firing and consumes it.
 */
public class QSTrapTriggerEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ITeam trapTeam;
    private final Player player;
    private final String trapName;
    private boolean cancelled;

    public QSTrapTriggerEvent(ITeam trapTeam, Player player, String trapName) {
        this.trapTeam = trapTeam;
        this.player = player;
        this.trapName = trapName;
    }

    public ITeam getTrapTeam() {
        return trapTeam;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * @return trap identifier as configured in upgrades2 (without the base-trap- prefix).
     */
    public String getTrapName() {
        return trapName;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
