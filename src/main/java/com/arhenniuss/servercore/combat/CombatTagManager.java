package com.arhenniuss.servercore.combat;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages PvP combat tagging. Players are tagged for 15 seconds
 * after dealing or receiving PvP damage.
 */
public class CombatTagManager {

    private static final long TAG_DURATION_MS = 15_000;

    /**
     * Combat tag record storing expiry and last attacker.
     * Future-proof for tracking tag reason, type, etc.
     */
    public record CombatTag(long expiresAt, UUID lastAttacker) {
    }

    private final Map<UUID, CombatTag> taggedPlayers;

    public CombatTagManager() {
        this.taggedPlayers = new HashMap<>();
    }

    /**
     * Tags a player as in combat, recording the attacker.
     */
    public void tag(Player player, UUID attacker) {
        taggedPlayers.put(player.getUniqueId(),
                new CombatTag(System.currentTimeMillis() + TAG_DURATION_MS, attacker));
    }

    /**
     * Checks whether the player is currently combat-tagged.
     */
    public boolean isTagged(Player player) {
        return isTagged(player.getUniqueId());
    }

    /**
     * Checks whether a UUID is currently combat-tagged.
     */
    public boolean isTagged(UUID uuid) {
        CombatTag tag = taggedPlayers.get(uuid);
        if (tag == null)
            return false;
        if (System.currentTimeMillis() >= tag.expiresAt()) {
            taggedPlayers.remove(uuid);
            return false;
        }
        return true;
    }

    /**
     * Returns the combat tag for a player, or null if not tagged/expired.
     */
    public CombatTag getTag(UUID uuid) {
        CombatTag tag = taggedPlayers.get(uuid);
        if (tag == null)
            return null;
        if (System.currentTimeMillis() >= tag.expiresAt()) {
            taggedPlayers.remove(uuid);
            return null;
        }
        return tag;
    }

    /**
     * Returns remaining combat tag seconds, or 0 if not tagged.
     */
    public int getRemainingSeconds(Player player) {
        CombatTag tag = taggedPlayers.get(player.getUniqueId());
        if (tag == null)
            return 0;
        long remaining = tag.expiresAt() - System.currentTimeMillis();
        return remaining > 0 ? (int) Math.ceil(remaining / 1000.0) : 0;
    }

    /**
     * Whether the logout penalty should be applied.
     * Returns true if the player is still combat-tagged.
     */
    public boolean shouldApplyLogoutPenalty(UUID uuid) {
        return isTagged(uuid);
    }

    /**
     * Clears the combat tag for a player. Called on quit after penalty applied.
     */
    public void clearTag(UUID uuid) {
        taggedPlayers.remove(uuid);
    }
}
