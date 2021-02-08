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

package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class EnableDisableCommand implements CommandExecutor {

    private TTRCore plugin;

    public EnableDisableCommand(TTRCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender theSender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("ttrenable")) {
            plugin.getConfigManager().setEnableOnStart(true);
            theSender.sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.GREEN + "Plugin enabled on server start. /reload or restart server to apply changes! Players should rejoin...");
        }
        if (label.equalsIgnoreCase("ttrdisable")) {
            plugin.getConfigManager().setEnableOnStart(false);
            theSender.sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + "Plugin disabled on server start. /reload or restart server to apply changes!");
        }
        if (label.equalsIgnoreCase("m")) {
            String message = ChatColor.GOLD + "~~~~" + ChatColor.WHITE + theSender.getName() + " > " + String.join(" ", args);
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.isOp()) {
                    p.sendMessage(message);
                }
            }
        }
        if (label.equalsIgnoreCase("check")) {
            String result = ChatColor.BLUE + "Check de teams";
            ArrayList<String> temp = new ArrayList<>();
            for (TTRTeam t : plugin.getTeamHandler().getTeams()) {
                temp.clear();
                for (UUID uid : t.getPlayers()) {
                    Player p = plugin.getServer().getPlayer(uid);
                    temp.add(p != null ? p.getName() : "none");
                }
                result += "\n" + ChatColor.AQUA + t.getIdentifier() + ": " + String.join(",", temp);
            }
            result += "\n" + ChatColor.AQUA + "No team: ";
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (plugin.getTeamHandler().getPlayerTeam(p) == null) {
                    result += p.getName() + " ";
                }
            }
            theSender.sendMessage(result);
        }
        return false;
    }
}