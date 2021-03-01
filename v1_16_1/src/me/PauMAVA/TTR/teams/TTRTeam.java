/*
 * TheTowersRemastered (TTR)
 * Copyright (c) 2019-2020  Pau Machetti Vallverdu
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.PauMAVA.TTR.teams;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TTRTeam {

    private String identifier;
    private List<UUID> players = new ArrayList<UUID>();
    private int points = 0;
    private Team teamBukkit;

    public TTRTeam(String identifier, List<UUID> players) {
        this.identifier = identifier;
        this.players = players;
    }

    public TTRTeam(String identifier) {
        this.identifier = identifier;
        teamBukkit = TTRCore.getInstance().getScoreboard().ttrScoreboard.registerNewTeam(identifier);
        teamBukkit.setAllowFriendlyFire(false);
        teamBukkit.setCanSeeFriendlyInvisibles(true);
        teamBukkit.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void addPlayer(UUID player) {
        this.players.add(player);
        teamBukkit.addPlayer(TTRCore.getInstance().getServer().getOfflinePlayer(player));
    }

    public void removePlayer(UUID player) {
        this.players.remove(player);
    }

    public List<UUID> getPlayers() {
        return this.players;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public int getPoints() {
        return this.points;
    }
}
