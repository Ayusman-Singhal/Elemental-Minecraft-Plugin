package com.arhenniuss.servercore.combat;

import com.arhenniuss.servercore.ability.AbilityType;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which player caused ability-based damage for kill credit and logging.
 * Records expire after 10 seconds.
 */
public class DamageAttributionManager {

    private static final long EXPIRY_MS = 10_000;

    /**
     * Immutable damage record linking an attacker to their ability.
     * Uses expiresAt instead of timestamp for cleaner expiry checks.
     */
    public record DamageRecord(UUID attacker, AbilityType abilityType, long expiresAt) {
    }

    private final ConcurrentHashMap<UUID, DamageRecord> victimMap;

    public DamageAttributionManager() {
        this.victimMap = new ConcurrentHashMap<>();
    }

    /**
     * Records that the attacker dealt ability damage to the victim.
     */
    public void recordDamage(UUID attacker, UUID victim, AbilityType abilityType) {
        victimMap.put(victim, new DamageRecord(attacker, abilityType,
                System.currentTimeMillis() + EXPIRY_MS));
    }

    /**
     * Returns the damage attribution for a victim if the record has not expired.
     */
    public DamageRecord getAttribution(UUID victim) {
        DamageRecord record = victimMap.get(victim);
        if (record == null)
            return null;
        if (System.currentTimeMillis() >= record.expiresAt()) {
            victimMap.remove(victim);
            return null;
        }
        return record;
    }

    /**
     * Clears attribution for a specific victim (e.g., on death).
     */
    public void clearAttribution(UUID victim) {
        victimMap.remove(victim);
    }

    /**
     * Clears all records involving a player (as victim). Called on quit.
     */
    public void clearAll(UUID uuid) {
        victimMap.remove(uuid);
    }

    /**
     * Removes expired records. Called periodically (e.g., every 60 seconds)
     * to prevent memory growth from victims that never died.
     */
    public void cleanupExpired() {
        long now = System.currentTimeMillis();
        victimMap.entrySet().removeIf(entry -> now >= entry.getValue().expiresAt());
    }
}
