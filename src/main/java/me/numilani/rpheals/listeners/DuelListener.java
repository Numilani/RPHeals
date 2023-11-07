package me.numilani.rpheals.listeners;

import me.numilani.rpheals.RPHeal;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.sql.SQLException;

public class DuelListener implements Listener {

    private RPHeal plugin;

    public DuelListener(RPHeal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClickPlayer(PlayerInteractAtEntityEvent event) throws SQLException {
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.WOODEN_SWORD && event.getRightClicked() instanceof Player){
            if (event.getPlayer().isSneaking()){
                if (plugin.dataSource.getDuel(event.getRightClicked().getUniqueId().toString()) != null){
                    plugin.dataSource.acceptDuel(
                            event.getRightClicked().getUniqueId().toString(),
                            event.getPlayer().getUniqueId().toString(),
                            (int)Math.round(event.getPlayer().getHealth()),
                            (int)Math.round(((Player)event.getRightClicked()).getHealth()));
                    event.getPlayer().sendMessage("Duel Accepted! FIGHT!");
                    event.getRightClicked().sendMessage("Duel accepted! FIGHT!");
                }
                else{
                    if (plugin.dataSource.playerInActiveDuel(event.getPlayer().getUniqueId().toString()) || plugin.dataSource.playerInActiveDuel(event.getPlayer().getUniqueId().toString())){
                        event.getPlayer().sendMessage("You cannot initiate a duel with this person right now!");
                        return;
                    }
                    plugin.dataSource.createDuel(event.getPlayer().getUniqueId().toString(), event.getRightClicked().getUniqueId().toString());
                    event.getPlayer().sendMessage("Duel request sent! Opponent has 60 seconds to respond.");
                    event.getRightClicked().sendMessage(String.format("You have received a duel request from %s. Shift-rightclick with a shield to accept it, or use /cancelduel to reject.", event.getPlayer().getDisplayName()));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        try {
                            // this logic feels weird, but I'm too tired to tell if it's wrong
                            var duelA = plugin.dataSource.getDuel(event.getPlayer().getUniqueId().toString());
                            var duelB = plugin.dataSource.getDuel(event.getRightClicked().getUniqueId().toString());
                            if ((duelA.PlayerOneConfirm && duelB.PlayerTwoConfirm) || (duelB.PlayerOneConfirm && duelB.PlayerOneConfirm)){
                                return;
                            }
                            plugin.dataSource.removeDuel(event.getPlayer().getUniqueId().toString(), event.getRightClicked().getUniqueId().toString());
                            event.getPlayer().sendMessage("Duel cancelled due to timeout.");
                            event.getRightClicked().sendMessage("Duel cancelled due to timeout.");
                        } catch (SQLException e) {
                            plugin.getLogger().warning(String.format("Duel was cancelled, but may have encountered problems: %s", e.getMessage()));
                        }
                    }, 20L * 60L);
                }
            }
        }
    }

    @EventHandler
    public void onDeathDuringDuel(EntityDamageByEntityEvent event) throws SQLException{
        if (event.getEntity() instanceof Player && ((Player) event.getEntity()).getHealth() - event.getFinalDamage() <= 0){
            var loser = (Player)event.getEntity();
            if (plugin.dataSource.playerInActiveDuel(event.getEntity().getUniqueId().toString())){
                event.setCancelled(true);
                var duel = plugin.dataSource.getDuel(loser.getUniqueId().toString());
                plugin.dataSource.removeDuel(duel.Id);
            }
            loser.sendMessage("You have lost the duel!");
            if (event.getDamager() instanceof Player){
                event.getDamager().sendMessage("You have won the duel!");
            }
        }

    }
}
