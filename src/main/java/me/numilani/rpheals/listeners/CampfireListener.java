package me.numilani.rpheals.listeners;

import me.numilani.rpheals.RPHeal;
import org.bukkit.Material;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import static java.lang.Math.abs;

public class CampfireListener implements Listener {
    private RPHeal plugin;

    public CampfireListener(RPHeal plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) throws SQLException{
        if (!plugin.dataSource.playerExistsInTable(event.getPlayer().getUniqueId().toString())){
            plugin.dataSource.initPlayer(event.getPlayer().getUniqueId().toString());
        }
    }

    @EventHandler
    public void onUseCampfire(PlayerInteractEvent event) throws SQLException {
        var idString = event.getPlayer().getUniqueId().toString();
        if (event.getClickedBlock() != null
                && event.getHand() == EquipmentSlot.HAND
                && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR
                && (event.getClickedBlock().getType() == Material.CAMPFIRE || event.getClickedBlock().getType() == Material.SOUL_CAMPFIRE)
                && ((Campfire)event.getClickedBlock().getBlockData()).isLit()){
            if (event.getPlayer().isSneaking()){
                if (GetTimeUntilNextCampfireHeal(event.getPlayer().getUniqueId().toString()).toMinutes() >= 0){
                    event.getPlayer().sendMessage("You rest by the campfire. It's soothing heat helps revitalize you.");
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60 * 20, 0, false, false, true));
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60 * 20, 2, false, false, true));
                    plugin.dataSource.updatePlayerCampfireInteractionTime(event.getPlayer().getUniqueId().toString());
                }
                else{
                    event.getPlayer().sendMessage(String.format("The warm glow of the campfire is inviting, but does nothing to help you. (Time left: %s mins)", abs(GetTimeUntilNextCampfireHeal(event.getPlayer().getUniqueId().toString()).toMinutes())));
                }
            }
        }
    }

    private Duration GetTimeUntilNextCampfireHeal(String playerId) throws SQLException {
        var lastHeal = plugin.dataSource.getPlayerCampfireInteractionTime(playerId);

        var timeNow = LocalDateTime.now(Clock.systemUTC()).minusMinutes(plugin.cfg.get("cooldownInterval", 30));

        return Duration.between(lastHeal, timeNow);
    }
}
