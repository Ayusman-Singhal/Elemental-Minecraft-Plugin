package com.arhenniuss.servercore.combat.status;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages conceptual elemental statuses on entities.
 *
 * Responsibilities:
 * - Apply status to entity (with Bukkit effect as implementation detail)
 * - Track duration per entity
 * - Prevent stacking (refresh only if new duration is longer)
 * - Remove expired statuses
 * - Provide hasStatus() for future ReactionEngine (Phase 6)
 *
 * PotionEffects and fire ticks are implementation details —
 * callers only deal with StatusType and duration.
 */
public class StatusService {

    /** Active statuses per entity UUID. */
    private final Map<UUID, Map<StatusType, StatusInstance>> activeStatuses = new ConcurrentHashMap<>();

    /**
     * Applies a conceptual status to a living entity.
     * If the entity already has this status with a longer remaining duration,
     * the call is a no-op (no downgrade).
     *
     * @param target        the entity to apply the status to
     * @param type          the status type
     * @param durationTicks the duration in game ticks (20 ticks = 1 second)
     */
    public void applyStatus(LivingEntity target, StatusType type, int durationTicks) {
        UUID uuid = target.getUniqueId();
        long durationMs = durationTicks * 50L; // 1 tick = 50ms
        long endTime = System.currentTimeMillis() + durationMs;

        Map<StatusType, StatusInstance> entityStatuses = activeStatuses.computeIfAbsent(uuid,
                k -> new EnumMap<>(StatusType.class));

        // Don't downgrade — only refresh if new duration is longer
        StatusInstance existing = entityStatuses.get(type);
        if (existing != null && !existing.isExpired() && existing.endTimeMs() >= endTime) {
            return;
        }

        StatusInstance instance = new StatusInstance(type, endTime, durationTicks);
        entityStatuses.put(type, instance);

        // Apply the Bukkit implementation
        applyBukkitEffect(target, type, durationTicks);
    }

    /**
     * Checks if an entity currently has the given status.
     * Used by the future ReactionEngine to detect cross-element interactions.
     *
     * @param target the entity to check
     * @param type   the status type to check for
     * @return true if the entity has an active (non-expired) instance of the status
     */
    public boolean hasStatus(LivingEntity target, StatusType type) {
        Map<StatusType, StatusInstance> entityStatuses = activeStatuses.get(target.getUniqueId());
        if (entityStatuses == null)
            return false;
        StatusInstance instance = entityStatuses.get(type);
        return instance != null && !instance.isExpired();
    }

    /**
     * Returns a snapshot of all active (non-expired) statuses on an entity.
     * Used by AbilityService to build ReactionContext.statusesBefore.
     *
     * @param target the entity to snapshot
     * @return immutable set of active status types (empty if none)
     */
    public Set<StatusType> getActiveStatuses(LivingEntity target) {
        Map<StatusType, StatusInstance> entityStatuses = activeStatuses.get(target.getUniqueId());
        if (entityStatuses == null)
            return EnumSet.noneOf(StatusType.class);

        EnumSet<StatusType> result = EnumSet.noneOf(StatusType.class);
        for (Map.Entry<StatusType, StatusInstance> entry : entityStatuses.entrySet()) {
            if (!entry.getValue().isExpired()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Removes a specific status from an entity immediately.
     * Cleans up the underlying Bukkit effect.
     *
     * @param target the entity
     * @param type   the status to remove
     */
    public void removeStatus(LivingEntity target, StatusType type) {
        Map<StatusType, StatusInstance> entityStatuses = activeStatuses.get(target.getUniqueId());
        if (entityStatuses != null) {
            entityStatuses.remove(type);
        }

        // Clean up Bukkit side
        removeBukkitEffect(target, type);
    }

    /**
     * Cleanup tick — removes expired statuses from tracking.
     * Should be called periodically (e.g. every 20 ticks).
     * Does NOT need to remove Bukkit effects since they expire naturally.
     */
    public void tick() {
        activeStatuses.entrySet().removeIf(entry -> {
            entry.getValue().entrySet().removeIf(statusEntry -> statusEntry.getValue().isExpired());
            return entry.getValue().isEmpty();
        });
    }

    /**
     * Cleans up all tracked statuses for an entity (e.g., on disconnect).
     */
    public void clearEntity(UUID uuid) {
        activeStatuses.remove(uuid);
    }

    // ── Bukkit Implementation Details ──

    private void applyBukkitEffect(LivingEntity target, StatusType type, int durationTicks) {
        if (type.usesFireTicks()) {
            // Fire-tick based status (BURNING)
            target.setFireTicks(durationTicks);
        } else if (type.getPotionEffect() != null) {
            // Potion-based status (WET, STUNNED, ROOTED, SLOWED)
            target.addPotionEffect(new PotionEffect(
                    type.getPotionEffect(),
                    durationTicks,
                    type.getAmplifier(),
                    true, // ambient
                    true, // particles
                    true // icon
            ));
        }
    }

    private void removeBukkitEffect(LivingEntity target, StatusType type) {
        if (type.usesFireTicks()) {
            target.setFireTicks(0);
        } else if (type.getPotionEffect() != null) {
            target.removePotionEffect(type.getPotionEffect());
        }
    }
}
