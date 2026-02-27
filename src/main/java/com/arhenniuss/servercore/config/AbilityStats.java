package com.arhenniuss.servercore.config;

/**
 * Immutable record holding all numeric balancing values for a single ability.
 * Fields that don't apply to a given ability type are set to their defaults.
 *
 * Mobility fields (velocityMagnitude, velocityY, immunityTicks) are 0 for
 * non-mobility abilities. When velocityMagnitude > 0, AbilityService treats
 * the ability as self-targeted mobility.
 */
public record AbilityStats(
        long cooldownMs,
        double damage,
        double radius,
        double knockback,
        double reach,
        double velocityMagnitude,
        double velocityY,
        int immunityTicks) {

    /**
     * Creates AbilityStats with sensible defaults for unused fields.
     * Velocity/immunity default to 0 (non-mobility).
     */
    public static AbilityStats of(long cooldownMs, double damage) {
        return new AbilityStats(cooldownMs, damage, 0, 0, 3.0, 0, 0, 0);
    }

    /**
     * Whether this ability is a mobility ability (has non-zero velocity).
     */
    public boolean isMobility() {
        return velocityMagnitude > 0 || velocityY > 0;
    }
}
