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

package me.PauMAVA.TTR.util;

import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.Cage;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.teams.TTRTeamHandler;
import me.PauMAVA.TTR.ui.TeamSelector;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ListIterator;
import java.util.UUID;

public class EventListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (TTRCore.getInstance().enabled()) {
            event.setJoinMessage(TTRPrefix.TTR_GAME + "" + ChatColor.GREEN + "+ " + ChatColor.GRAY + event.getPlayer().getName() + " se ha conectado");
            if (TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.PREGAME) {
                event.getPlayer().setGameMode(GameMode.ADVENTURE);
                Inventory playerInventory = event.getPlayer().getInventory();
                playerInventory.clear();
                playerInventory.setItem(0, new ItemStack(Material.BLACK_BANNER));
                Location location = TTRCore.getInstance().getConfigManager().getLobbyLocation();
                Location copy = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
                copy.add(location.getX() > 0 ? 0.5 : 0.5, 0.0, location.getZ() > 0 ? 0.5 : -0.5);
                event.getPlayer().teleport(copy);
                TTRCore.getInstance().getAutoStarter().addPlayerToQueue(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (TTRCore.getInstance().enabled()) {
            event.setQuitMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + "- " + ChatColor.GRAY + event.getPlayer().getName() + " se ha desconectado");
//            TTRCore.getInstance().getAutoStarter().removePlayerFromQueue(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDropEvent(PlayerDropItemEvent event) {
        if (TTRCore.getInstance().enabled() && !TTRCore.getInstance().getCurrentMatch().isOnCourse()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerClickEvent(PlayerInteractEvent event) {
        if (TTRCore.getInstance().enabled() && !(TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.INGAME)) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            event.setCancelled(true);
            if (event.getItem() != null && event.getItem().getType() == Material.BLACK_BANNER) {
                new TeamSelector(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void placeBlockEvent(BlockPlaceEvent event) {
        if (TTRCore.getInstance().enabled() && !(TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.INGAME)) {
            event.getPlayer().sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + "No puedes construir aqui!");
            event.setCancelled(true);
            return;
        }
        Location b = event.getBlockPlaced().getLocation();
        for (Cage cage : TTRCore.getInstance().getCurrentMatch().checker.cages) {
            Location c = cage.getLocation();
            if ((c.getX() + 3) >= b.getX() && (c.getX() - 3) <= b.getX() &&
                    (c.getZ() + 3) >= b.getZ() && (c.getZ() - 3) <= b.getZ() &&
                    203 >= b.getY() && 189 <= b.getY()
            ) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void breakBlockEvent(BlockBreakEvent event) {
        if (TTRCore.getInstance().enabled() && !TTRCore.getInstance().getCurrentMatch().isOnCourse()) {
            event.getPlayer().sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + "No puedes romper ese bloque!");
            event.setCancelled(true);
        }
        if (event.getBlock().getType().equals(Material.COBWEB)) return;
        for (Cage cage : TTRCore.getInstance().getCurrentMatch().checker.cages) {
            Location c = cage.getLocation();
            Location b = event.getBlock().getLocation();
            if ((c.getX() + 3) >= b.getX() && (c.getX() - 3) <= b.getX() &&
                    (c.getZ() + 3) >= b.getZ() && (c.getZ() - 3) <= b.getZ() &&
                    203 >= b.getY() && 189 <= b.getY()
            ) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (TTRCore.getInstance().enabled() && TTRCore.getInstance().getCurrentMatch().isOnCourse()) {
            ListIterator<ItemStack> iter = event.getDrops().listIterator();
            while (iter.hasNext()) {
                if (iter.next().getType().equals(Material.BEACON)) {
                    iter.remove();
                    event.getEntity().getLocation().clone().set(0, 203, 1152).getBlock().setType(Material.BEACON);
                }
            }
            TTRCore.getInstance().getCurrentMatch().playerDeath(event.getEntity(), event.getEntity().getKiller());
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (TTRCore.getInstance().getCurrentMatch().getStatus() != MatchStatus.INGAME) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType().equals(EntityType.PLAYER)) {
            TTRTeamHandler th = TTRCore.getInstance().getTeamHandler();
            Player p1 = (Player) event.getEntity();
            TTRTeam th1 = th.getPlayerTeam(p1);
            if (event.getDamager().getType().equals(EntityType.PLAYER)) {
                Player p2 = (Player) event.getDamager();
                TTRTeam th2 = th.getPlayerTeam(p2);
                if (th1 != null & th2 != null && th1.equals(th2)) {
                    event.setCancelled(true);
                }
            }
            if (event.getDamager() instanceof Projectile) {
                Projectile bullet = (Projectile) event.getDamager();
                if (bullet.getShooter() instanceof Player) {
                    Player p2 = (Player) bullet.getShooter();
                    TTRTeam th2 = th.getPlayerTeam(p2);
                    if (th1 != null & th2 != null && th1.equals(th2)) {
                        event.setCancelled(true);
                    }
                }
            }
        }

    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
        ((Player) event.getEntity()).setSaturation(20);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        TTRTeam playerTeam = TTRCore.getInstance().getTeamHandler().getPlayerTeam(event.getPlayer());
        ChatColor teamcolor = ChatColor.GRAY;
        if (playerTeam != null) {
            teamcolor = TTRCore.getInstance().getConfigManager().getTeamColor(playerTeam.getIdentifier());
        }
        if (event.getMessage().startsWith("!") || (TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.PREGAME)) {
            event.setFormat(TTRPrefix.TTR_GLOBAL + "" + teamcolor + " %1$s" + ChatColor.WHITE + " > %2$s");
        } else {
            event.getRecipients().clear();
            if (playerTeam != null) {
                for (Player p : TTRCore.getInstance().getServer().getOnlinePlayers()) {
                    if (playerTeam.getPlayers().contains(p.getUniqueId())) {
                        p.sendMessage(TTRPrefix.TTR_TEAM + "" + teamcolor + event.getPlayer().getName() + ChatColor.WHITE + " > " + event.getMessage());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBeacon(BeaconEffectEvent event) {
        Location b = event.getBlock().getLocation();
        TTRTeam playerTeam = TTRCore.getInstance().getTeamHandler().getPlayerTeam(event.getPlayer());
        event.setCancelled(true);
        if (playerTeam != null) {
            ChatColor color = TTRCore.getInstance().getConfigManager().getTeamColor(playerTeam.getIdentifier());
            if (b.getX() == -49 && b.getY() == 205 && b.getZ() == 1152) {
                //blue
                if (color.equals(ChatColor.BLUE)) {
                    event.setCancelled(false);
                }
            }
            if (b.getX() == 49 && b.getY() == 205 && b.getZ() == 1152) {
                //red
                if (color.equals(ChatColor.RED)) {
                    event.setCancelled(false);
                }
            }
        }
    }

}
