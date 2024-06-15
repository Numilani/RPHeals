package me.numilani.rpheals.listeners;

import me.numilani.rpheals.RPHeal;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class DuelListener implements Listener {

    private RPHeal plugin;

    public DuelListener(RPHeal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClickPlayer(PlayerInteractEntityEvent event) throws SQLException {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        // If the player tries to start/respond to a duel
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.WOODEN_SWORD
                && event.getRightClicked() instanceof Player
                && event.getPlayer().isSneaking()){
            var activeDuel = plugin.dataSource.getDuel(event.getRightClicked().getUniqueId().toString());

            // If there's already a duel active, don't make a new one
            if (plugin.dataSource.playerInActiveDuel(event.getPlayer().getUniqueId().toString()) || plugin.dataSource.playerInActiveDuel(event.getPlayer().getUniqueId().toString())){
                event.getPlayer().sendMessage("You cannot initiate a duel with this person right now!");
                return;
            }

            // If that player has no duel active, make one
            if (activeDuel == null){
                var duel = plugin.dataSource.createDuel(event.getPlayer().getUniqueId().toString(), event.getRightClicked().getUniqueId().toString());
                event.getPlayer().sendMessage("Duel request sent! Opponent has 60 seconds to respond.");
                event.getRightClicked().sendMessage(String.format("You have received a duel request from %s. Shift-rightclick with a wooden sword to accept it, or wait 60 seconds to reject.", event.getPlayer().getDisplayName()));
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    try {
                        if (plugin.dataSource.playerInActiveDuel(event.getPlayer().getUniqueId().toString()) || plugin.dataSource.playerInActiveDuel(event.getRightClicked().getUniqueId().toString())){
                            return;
                        }
                        plugin.dataSource.removeDuel(duel.Id);
//                        event.getPlayer().sendMessage("Duel cancelled due to timeout.");
//                        event.getRightClicked().sendMessage("Duel cancelled due to timeout.");
                    } catch (SQLException e) {
                        plugin.getLogger().warning(String.format("Duel was cancelled, but may have encountered problems: %s", e.getMessage()));
                    }
                }, 20L * 60L);
                return;
            }
            // If there IS an active duel request, accept it
            else{
                plugin.dataSource.acceptDuel(
                        event.getRightClicked().getUniqueId().toString(),
                        event.getPlayer().getUniqueId().toString(),
                        (int)Math.round(event.getPlayer().getHealth()),
                        (int)Math.round(((Player)event.getRightClicked()).getHealth()));
                event.getPlayer().sendMessage("Duel Accepted! FIGHT!");
                event.getRightClicked().sendMessage("Duel accepted! FIGHT!");
                return;
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) throws SQLException{
        if (plugin.dataSource.playerInActiveDuel(event.getPlayer().getUniqueId().toString())){
            var duel = plugin.dataSource.getDuel(event.getPlayer().getUniqueId().toString());
            plugin.dataSource.removeDuel(duel.Id);

            var p1 = plugin.getServer().getPlayer(UUID.fromString(duel.PlayerOneId));
            var p2 = plugin.getServer().getPlayer(UUID.fromString(duel.PlayerTwoId));

            if (p1 != null){
                p1.sendTitle("", "Duel was cancelled due to disconnect!", 20, 80, 20);
            }
            if (p2 != null){
                p2.sendTitle("", "Duel was cancelled due to disconnect!", 20, 80, 20);
            }
        }
    }

    @EventHandler
    public void onDeathDuringDuel(EntityDamageEvent event) throws SQLException{
        // If a player dies
        if (event.getEntity() instanceof Player loser && ((Player) event.getEntity()).getHealth() - event.getFinalDamage() <= 0){
            // if the player was in a duel
            if (plugin.dataSource.playerInActiveDuel(event.getEntity().getUniqueId().toString())){
                event.setCancelled(true);
                var duel = plugin.dataSource.getDuel(loser.getUniqueId().toString());

                Player winner = plugin.getServer().getPlayer(UUID.fromString(duel.PlayerOneId)) != loser ? plugin.getServer().getPlayer(UUID.fromString(duel.PlayerOneId)) : plugin.getServer().getPlayer(UUID.fromString(duel.PlayerTwoId));

                if (Objects.equals(loser.getUniqueId().toString(), duel.PlayerOneId)){
                    loser.setHealth(duel.PlayerOneHealth);
                }
                else{
                    loser.setHealth(duel.PlayerTwoHealth);
                }

                loser.sendTitle("", "You lost the duel!", 20,80,20);
                loser.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 9, false, false,true));
                loser.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 9, false, false,true));
                loser.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 9, false, false,true));

                if (winner != null){
                    if (Objects.equals(winner.getUniqueId().toString(), duel.PlayerOneId)){
                        winner.setHealth(duel.PlayerOneHealth);
                    }
                    else{
                        winner.setHealth(duel.PlayerTwoHealth);
                    }

                    winner.sendTitle("", "You won the duel!", 20,80,20);
                    winner.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 9, false, false,true));
                    winner.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 9, false, false,true));
                    winner.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 9, false, false,true));
                }

                plugin.dataSource.removeDuel(duel.Id);
            }
        }
    }
}
