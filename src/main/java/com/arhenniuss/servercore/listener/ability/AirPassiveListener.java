package com.arhenniuss.servercore.listener.ability;

import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.player.PlayerDataManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Air Passive: Light Frame.
 * +5% movement speed via attribute modifier (applied on join, removed on quit).
 * 20% fall damage reduction via EntityDamageEvent.
 *
 * This listener is PASSIVE ONLY — it does not deal damage.
 */
public class AirPassiveListener implements Listener {

    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String SPEED_MODIFIER_NAME = "air_light_frame";
    private static final double SPEED_BONUS = 0.05; // 5% of base 0.1 = 0.005
    private static final double FALL_REDUCTION = 0.80; // 20% reduction

    private final PlayerDataManager playerDataManager;

    public AirPassiveListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    /**
     * Apply speed modifier when Air player joins.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Element element = playerDataManager.getElement(player.getUniqueId());
        if (element != Element.AIR)
            return;

        applySpeedBonus(player);
    }

    /**
     * Remove speed modifier when player quits (cleanup).
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeSpeedBonus(event.getPlayer());
    }

    /**
     * Reduce fall damage by 20% for Air players.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        Element element = playerDataManager.getElement(player.getUniqueId());
        if (element != Element.AIR)
            return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setDamage(event.getDamage() * FALL_REDUCTION);
        }
    }

    /**
     * Applies the +5% speed attribute modifier.
     */
    public void applySpeedBonus(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr == null)
            return;

        // Remove existing modifier first (idempotent)
        for (AttributeModifier mod : attr.getModifiers()) {
            if (mod.getUniqueId().equals(SPEED_MODIFIER_UUID)) {
                attr.removeModifier(mod);
            }
        }

        attr.addModifier(new AttributeModifier(
                SPEED_MODIFIER_UUID,
                SPEED_MODIFIER_NAME,
                SPEED_BONUS,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1));
    }

    /**
     * Removes the speed attribute modifier.
     */
    public void removeSpeedBonus(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr == null)
            return;

        for (AttributeModifier mod : attr.getModifiers()) {
            if (mod.getUniqueId().equals(SPEED_MODIFIER_UUID)) {
                attr.removeModifier(mod);
            }
        }
    }
}
