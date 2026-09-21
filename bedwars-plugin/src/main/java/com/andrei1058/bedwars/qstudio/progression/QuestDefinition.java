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

package com.andrei1058.bedwars.qstudio.progression;

import java.util.List;

/**
 * Quest definition loaded from progression.quest-list.
 */
public class QuestDefinition {

    /** objective types. */
    public static final String WINS = "wins";
    public static final String BEDS_BROKEN = "beds_broken";
    public static final String FINAL_KILLS = "final_kills";
    public static final String KILLS = "kills";
    public static final String GAMES_PLAYED = "games_played";
    public static final String RESOURCES_COLLECTED = "resources_collected";

    /** frequencies. */
    public static final String DAILY = "daily";
    public static final String WEEKLY = "weekly";
    public static final String ONCE = "once";

    private final String id;
    private final String type;
    private final String frequency;
    private final int amount;
    private final int rewardXp;
    private final List<String> rewardCommands;

    public QuestDefinition(String id, String type, String frequency, int amount, int rewardXp, List<String> rewardCommands) {
        this.id = id;
        this.type = type;
        this.frequency = frequency;
        this.amount = amount;
        this.rewardXp = rewardXp;
        this.rewardCommands = rewardCommands;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getFrequency() {
        return frequency;
    }

    public int getAmount() {
        return amount;
    }

    public int getRewardXp() {
        return rewardXp;
    }

    public List<String> getRewardCommands() {
        return rewardCommands;
    }
}
