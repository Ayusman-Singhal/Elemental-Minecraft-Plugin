package com.arhenniuss.servercore.listener.ability;

import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.player.PlayerDataManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles landing damage from double jump (mobility) abilities.
 * When a player lands (transitions from air to ground) shortly after using mobility,
 * it applies AOE damage to nearby entities.
 *
 * Landing damage scales:
 * - FIRE: 4.0 damage, 3.0 block radius
 * - WATER: 3.0 damage, 2.5 block radius
 * - EARTH: 5.0 damage, 3.5 block radius
 * - AIR: 3.5 damage, 3.0 block radius
 */
public class MobilityLandingListener implements Listener {

    private final PlayerDataManager playerDataManager;
    private final Map<UUID, Long> lastMobilityUseTime;
    private final Map<UUID, Boolean> wasAirborne;

    private static final long LANDING_WINDOW_MS = 2000; // 2 seconds to land and trigger damage
    private static final double FIRE_DAMAGE = 4.0;
    private static final double FIRE_RADIUS = 3.0;
    private static final double WATER_DAMAGE = 3.0;
    private static final double WATER_RADIUS = 2.5;
    private static final double EARTH_DAMAGE = 5.0;
    private static final double EARTH_RADIUS = 3.5;
    private static final double AIR_DAMAGE = 3.5;
    private static final double AIR_RADIUS = 3.0;

    public MobilityLandingListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
        this.lastMobilityUseTime = new HashMap<>();
        this.wasAirborne = new HashMap<>();
    }

    /**
     * Called when mobility ability is used to record timestamp.
     * Should be called from AbilityService.
     */
    public void onMobilityUsed(Player player) {
        lastMobilityUseTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Detects landing (air → ground transition) and applies damage.
     * Runs every move to check if player just landed.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Check if player has an element
        Element element = playerDataManager.getElement(uuid);
        if (element == null) {
            return;
        }

        boolean isCurrentlyAirborne = player.getVelocity().getY() < 0.0 || !isGrounded(player);
        boolean wasAir = wasAirborne.getOrDefault(uuid, false);

        // Detect landing: was airborne, now grounded
        if (wasAir && isGrounded(player)) {
            // Check if this is within the landing window of a mobility use
            Long mobilityTime = lastMobilityUseTime.get(uuid);
            if (mobilityTime != null) {
                long elapsed = System.currentTimeMillis() - mobilityTime;
                if (elapsed < LANDING_WINDOW_MS) {
                    // Apply landing damage
                    applyLandingDamage(player, element);
                    lastMobilityUseTime.remove(uuid);
                }
            }
        }

        wasAirborne.put(uuid, isCurrentlyAirborne);
    }

    /**
     * Applies AOE damage to entities near the landing location.
     */
    private void applyLandingDamage(Player player, Element element) {
        double damage;
        double radius;

        switch (element) {
            case FIRE:
                damage = FIRE_DAMAGE;
                radius = FIRE_RADIUS;
                break;
            case WATER:
                damage = WATER_DAMAGE;
                radius = WATER_RADIUS;
                break;
            case EARTH:
                damage = EARTH_DAMAGE;
                radius = EARTH_RADIUS;
                break;
            case AIR:
                damage = AIR_DAMAGE;
                radius = AIR_RADIUS;
                break;
            default:
                return;
        }

        // Get nearby entities within radius
        for (Entity entity : player.getLocation().getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity target = (LivingEntity) entity;

                // Skip player targets (only damage mobs)
                if (target instanceof Player) {
                    continue;
                }

                // Apply damage
                target.damage(damage, player);
            }
        }
    }

    /**
     * Checks if a player is grounded by sampling the block below their feet.
     */
    private boolean isGrounded(Player player) {
        return !player.getLocation().clone().subtract(0.0, 0.1, 0.0).getBlock().isPassable();
    }

    /**
     * Cleans up player data on quit.
     */
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastMobilityUseTime.remove(uuid);
        wasAirborne.remove(uuid);
    }
    
    /**
     * Cancels fall damage if the player recently used mobility.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerFallDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL) {
            UUID uuid = player.getUniqueId();
            Long mobilityTime = lastMobilityUseTime.get(uuid);
            
            // Allow up to 3 seconds of fall damage immunity after using a mobility ability
            if (mobilityTime != null && (System.currentTimeMillis() - mobilityTime < 3000)) {
                event.setCancelled(true);
            }
        }
    }
}
