package com.arhenniuss.servercore.combat.targeting;

import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs configurable-reach ray tracing for abilities.
 * Element-agnostic — usable by any element's abilities.
 * Filters self, spectators, and prevents targeting through walls.
 */
public class ReachHelper {

    /**
     * Finds the first valid LivingEntity target within the given reach.
     * Blocks are checked first — cannot hit through walls.
     *
     * @param player the casting player
     * @param reach  the maximum reach distance
     * @return the target entity, or null if no valid target
     */
    public LivingEntity findTarget(Player player, double reach) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();

        // First check if a block is in the way
        RayTraceResult blockHit = player.getWorld().rayTraceBlocks(
                eyeLocation, direction, reach, FluidCollisionMode.NEVER, true);

        double effectiveReach = reach;
        if (blockHit != null) {
            // Limit reach to the block hit distance
            effectiveReach = blockHit.getHitPosition().distance(eyeLocation.toVector());
        }

        // Now raycast for entities within the effective reach
        double finalReach = effectiveReach;
        RayTraceResult entityHit = player.getWorld().rayTraceEntities(
                eyeLocation, direction, finalReach, 0.5,
                entity -> entity instanceof LivingEntity
                        && !entity.equals(player)
                        && !(entity instanceof Player p && p.getGameMode() == GameMode.SPECTATOR));

        if (entityHit != null && entityHit.getHitEntity() instanceof LivingEntity target) {
            return target;
        }

        return null;
    }

    /**
     * Finds all LivingEntity targets within a cone from the player's eye location.
     * Useful for sweep/cleave-style abilities.
     *
     * @param player the casting player
     * @param reach  the cone length
     * @param angle  the cone half-angle in degrees
     * @return list of valid targets within the cone
     */
    public List<LivingEntity> findTargetsInCone(Player player, double reach, double angle) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();
        double cosAngle = Math.cos(Math.toRadians(angle));

        List<LivingEntity> targets = new ArrayList<>();

        for (var entity : player.getNearbyEntities(reach, reach, reach)) {
            if (!(entity instanceof LivingEntity living))
                continue;
            if (entity instanceof Player p && p.getGameMode() == GameMode.SPECTATOR)
                continue;

            Vector toEntity = entity.getLocation().toVector()
                    .subtract(eyeLocation.toVector()).normalize();
            double dot = direction.dot(toEntity);

            if (dot >= cosAngle) {
                // Check line of sight (no wall between)
                if (player.hasLineOfSight(entity)) {
                    targets.add(living);
                }
            }
        }

        return targets;
    }
}
