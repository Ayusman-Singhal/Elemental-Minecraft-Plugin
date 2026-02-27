package com.arhenniuss.servercore.ability;

import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    /**
     * Snapshot of cooldown remaining millis for all categories.
     * Used by CooldownDisplayService to avoid repeated map lookups.
     */
    public record CooldownSnapshot(long basicRemaining, long secondaryRemaining,
            long specialRemaining, long mobilityRemaining) {
    }

    private final Map<UUID, Map<CooldownCategory, Long>> cooldowns;

    public CooldownManager() {
        this.cooldowns = new HashMap<>();
    }

    /**
     * Checks if the player is on cooldown for the given ability's category.
     */
    public boolean isOnCooldown(Player player, Ability ability) {
        Map<CooldownCategory, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null)
            return false;

        Long expiry = playerCooldowns.get(ability.getCooldownCategory());
        if (expiry == null)
            return false;

        return System.currentTimeMillis() < expiry;
    }

    /**
     * Sets a cooldown for the player based on the ability's category and duration.
     */
    public void setCooldown(Player player, Ability ability) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new EnumMap<>(CooldownCategory.class))
                .put(ability.getCooldownCategory(),
                        System.currentTimeMillis() + ability.getCooldownMillis());
    }

    /**
     * Returns remaining cooldown time in milliseconds for the player and ability.
     */
    public long getRemainingMillis(Player player, Ability ability) {
        return getRemainingMillis(player, ability.getCooldownCategory());
    }

    /**
     * Returns remaining cooldown time in milliseconds for a specific category.
     */
    public long getRemainingMillis(Player player, CooldownCategory category) {
        Map<CooldownCategory, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null)
            return 0;

        Long expiry = playerCooldowns.get(category);
        if (expiry == null)
            return 0;

        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * Extends an existing cooldown by the given amount of milliseconds.
     * If no cooldown exists for the category, this is a no-op.
     */
    public void extendCooldown(Player player, CooldownCategory category, long additionalMs) {
        Map<CooldownCategory, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null)
            return;

        Long expiry = playerCooldowns.get(category);
        if (expiry == null || System.currentTimeMillis() >= expiry)
            return;

        playerCooldowns.put(category, expiry + additionalMs);
    }

    /**
     * Returns a snapshot of all cooldown remaining millis for the player.
     * Single map lookup, extracts all four categories at once.
     */
    public CooldownSnapshot getSnapshot(Player player) {
        Map<CooldownCategory, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return new CooldownSnapshot(0, 0, 0, 0);
        }

        long now = System.currentTimeMillis();
        return new CooldownSnapshot(
                remaining(playerCooldowns.get(CooldownCategory.BASIC), now),
                remaining(playerCooldowns.get(CooldownCategory.SECONDARY), now),
                remaining(playerCooldowns.get(CooldownCategory.SPECIAL), now),
                remaining(playerCooldowns.get(CooldownCategory.MOBILITY), now));
    }

    private long remaining(Long expiry, long now) {
        if (expiry == null)
            return 0;
        return Math.max(0, expiry - now);
    }

    /**
     * Clears all cooldowns for a player. Call on quit to prevent memory leaks.
     */
    public void clearAll(UUID uuid) {
        cooldowns.remove(uuid);
    }
}
