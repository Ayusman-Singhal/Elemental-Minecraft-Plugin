package com.arhenniuss.servercore.listener.ability;

import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.player.PlayerDataManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Earth Passive: Grounded Body — nullifies incoming knockback for Earth
 * players using GENERIC_KNOCKBACK_RESISTANCE attribute.
 *
 * This listener is PASSIVE ONLY — it does not deal damage.
 */
public class EarthPassiveListener implements Listener {

    private static final UUID KB_MODIFIER_UUID = UUID.fromString("b2c3d4e5-f678-90ab-cdef-123456789012");
    private static final String KB_MODIFIER_NAME = "earth_grounded_body";
    private static final double KB_RESISTANCE = 1.0; // 100% resistance

    private final PlayerDataManager playerDataManager;

    public EarthPassiveListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    /**
     * Apply knockback resistance when Earth player joins.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Element element = playerDataManager.getElement(player.getUniqueId());
        if (element != Element.EARTH)
            return;

        applyKnockbackResistance(player);
    }

    /**
     * Remove modifier when player quits (cleanup).
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeKnockbackResistance(event.getPlayer());
    }

    /**
     * Applies the 100% knockback resistance attribute modifier.
     */
    public void applyKnockbackResistance(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (attr == null)
            return;

        // Remove existing modifier first (idempotent)
        for (AttributeModifier mod : attr.getModifiers()) {
            if (mod.getUniqueId().equals(KB_MODIFIER_UUID)) {
                attr.removeModifier(mod);
            }
        }

        attr.addModifier(new AttributeModifier(
                KB_MODIFIER_UUID,
                KB_MODIFIER_NAME,
                KB_RESISTANCE,
                AttributeModifier.Operation.ADD_NUMBER));
    }

    /**
     * Removes the knockback resistance attribute modifier.
     */
    public void removeKnockbackResistance(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (attr == null)
            return;

        for (AttributeModifier mod : attr.getModifiers()) {
            if (mod.getUniqueId().equals(KB_MODIFIER_UUID)) {
                attr.removeModifier(mod);
                break;
            }
        }
    }
}
