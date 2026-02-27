package com.arhenniuss.servercore.listener.ability;

import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.player.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Cancels drowning damage for players with the Water element.
 * Mirrors FirePassiveListener pattern.
 */
public class WaterPassiveListener implements Listener {

    private final PlayerDataManager playerDataManager;

    public WaterPassiveListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Element element = playerDataManager.getElement(player.getUniqueId());
        if (element != Element.WATER) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            event.setCancelled(true);
        }
    }
}
