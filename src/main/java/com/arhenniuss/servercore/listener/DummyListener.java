package com.arhenniuss.servercore.listener;

import com.arhenniuss.servercore.combat.CooldownDisplayService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles Training Dummy interactions:
 * - Displays damage dealt to the attacker's action bar
 * - Temporarily suppresses cooldown HUD so damage text remains visible
 * - Prevents death (resets health)
 * - Prevents knockback (resets velocity)
 * - Tracks total damage per dummy session
 */
public class DummyListener implements Listener {

    private static final long DAMAGE_DISPLAY_SUPPRESS_MS = 900L;

    private final NamespacedKey dummyKey;
    private final JavaPlugin plugin;
    private final CooldownDisplayService cooldownDisplayService;

    public DummyListener(NamespacedKey dummyKey, JavaPlugin plugin,
            CooldownDisplayService cooldownDisplayService) {
        this.dummyKey = dummyKey;
        this.plugin = plugin;
        this.cooldownDisplayService = cooldownDisplayService;
    }

    /**
     * Checks if an entity is a tagged training dummy.
     */
    public boolean isDummy(org.bukkit.entity.Entity entity) {
        return entity instanceof ArmorStand stand
                && stand.getPersistentDataContainer().has(dummyKey, PersistentDataType.BYTE);
    }

    /**
     * Intercepts all damage to training dummies.
     * Displays damage to attacker, prevents death, resets velocity.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDummyDamage(EntityDamageEvent event) {
        if (!isDummy(event.getEntity()))
            return;

        ArmorStand dummy = (ArmorStand) event.getEntity();
        double damage = event.getFinalDamage();

        // Prevent death — cancel if it would kill
        if (dummy.getHealth() - damage <= 0) {
            event.setCancelled(true);
            double maxHealth = dummy.getAttribute(
                    org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            dummy.setHealth(maxHealth);
        }

        // Display damage to attacker (if player-caused)
        if (event instanceof EntityDamageByEntityEvent byEntity) {
            if (byEntity.getDamager() instanceof Player attacker) {
                // Reserve action-bar window for dummy damage display.
                cooldownDisplayService.suppressFor(attacker.getUniqueId(), DAMAGE_DISPLAY_SUPPRESS_MS);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    showDamageDisplay(attacker, damage);
                }, 1L);
            }
        }

        // Reset velocity on next tick to prevent knockback
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (dummy.isValid()) {
                dummy.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
            }
        }, 1L);
    }

    /**
     * Sends a formatted damage display to the attacker's action bar.
     */
    private void showDamageDisplay(Player attacker, double damage) {
        String formatted = String.format("%.1f", damage);

        Component display = Component.text()
                .append(Component.text("⚔ ", NamedTextColor.YELLOW))
                .append(Component.text(formatted, NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text(" damage", NamedTextColor.GRAY))
                .build();

        attacker.sendActionBar(display);
    }
}
