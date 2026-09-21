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

package com.andrei1058.bedwars.qstudio.qmessages;

import com.andrei1058.bedwars.api.language.Language;

import java.util.Collections;

/**
 * Default messages of the qStudio layer. Every language receives the
 * default value; translations can be edited in the language files.
 */
public final class QMessages {

    private QMessages() {
    }

    public static void registerDefaults() {
        save("qstudio.grapple-too-far", "&cNo hook target in range!");
        save("qstudio.beacon-not-ready", "&7Beacon is still activating...");
        save("qstudio.beacon-interrupted", "&cYour teleport beacon was interrupted!");
        save("qstudio.teleport-interrupted", "&cTeleport cancelled!");
        save("qstudio.teleport-channel", "&aTeleporting in &e{seconds}s... &7stand still!");
        save("qstudio.shield-activated", "&bTeam shield active for {seconds}s!");
        save("qstudio.trap-alarm", "&c&lALARM! &f{player} &7is on your island!");
        save("qstudio.forge-upgraded", "&6Forge upgraded to level {level}!");
        save("qstudio.generator-boost-start", "&6Generator boost! &7Double speed for {seconds}s!");
        save("qstudio.quest-progress", "&7Quest: &f{quest} &e{progress}/{amount}");
        save("qstudio.quest-completed", "&aQuest completed: &f{quest} &7- rewards delivered!");
        save("qstudio.achievement-unlocked", "&6Achievement unlocked: &f{achievement}!");
        save("qstudio.combat-tag-pearl", "&cYou cannot use pearls while in combat!");
        save("qstudio.purchase-limit-life", "&cPurchase limit reached for this life.");
        save("qstudio.purchase-limit-game", "&cPurchase limit reached for this game.");
        save("qstudio.purchase-cooldown", "&cSlow down! Purchase cooldown active.");
        save("qstudio.bed-protection-timer", "&bYour beds are protected for {seconds}s!");
        save("qstudio.bed-protection-over", "&cBed protection is over. Defend your bed!");
        save("qstudio.bed-broken-global", "&c{player} &7destroyed the &f{team} &7bed!");
    }

    private static void save(String path, String message) {
        Language.saveIfNotExists(path, message);
        Language.saveIfNotExists(path + "-list", Collections.singletonList(message));
    }
}
