package com.arhenniuss.servercore.listener.ability;

import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.player.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

/**
 * Earth Passive: Grounded Body — reduces incoming knockback by 20% for Earth
 * players.
 *
 * Listens to EntityDamageEvent at MONITOR priority (after damage is applied)
 * and scales the resulting velocity down by 20%.
 *
 * This listener is PASSIVE ONLY — it does not deal damage.
 */
public class EarthPassiveListener implements Listener {

    private static final double KB_REDUCTION = 0.80; // 20% reduction = 80% of original

    private final PlayerDataManager playerDataManager;

    public EarthPassiveListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    /**
     * Reduces knockback velocity for Earth players after damage is applied.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Element element = playerDataManager.getElement(player.getUniqueId());
        if (element != Element.EARTH) {
            return;
        }

        // Schedule velocity reduction for next tick (velocity is set after event)
        player.getServer().getScheduler().runTaskLater(
                player.getServer().getPluginManager()
                        .getPlugin("ServerCore"),
                () -> {
                    if (player.isOnline()) {
                        Vector velocity = player.getVelocity();
                        player.setVelocity(velocity.multiply(KB_REDUCTION));
                    }
                },
                1L // Next tick — after vanilla knockback is applied
        );
    }
}
