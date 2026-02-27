package com.arhenniuss.servercore.combat.status;

/**
 * Represents an active status effect on an entity.
 * Tracks the status type, expiration time, and intensity.
 */
public record StatusInstance(StatusType type, long endTimeMs, int durationTicks) {

    /**
     * Whether this status has expired.
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= endTimeMs;
    }

    /**
     * Remaining time in milliseconds.
     */
    public long remainingMs() {
        return Math.max(0, endTimeMs - System.currentTimeMillis());
    }
}
