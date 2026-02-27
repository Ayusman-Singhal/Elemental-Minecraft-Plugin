package com.arhenniuss.servercore.ability;

import com.arhenniuss.servercore.combat.DamageAttributionManager;
import com.arhenniuss.servercore.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * Context passed to ability execution. Contains all data an ability
 * needs for visual effects and metadata.
 *
 * Mobility fields (selfTargeted, selfVelocity, immunityTicks) are
 * populated by AbilityService from AbilityStats when the ability
 * is a mobility ability. Non-mobility abilities have these as defaults.
 */
public class AbilityContext {

    private final Player player;
    private final PlayerData playerData;
    private final JavaPlugin plugin;
    private final Location origin;
    private final long timestamp;
    private final DamageAttributionManager damageManager;

    // ── Mobility fields (populated from AbilityStats by AbilityService) ──
    private final boolean selfTargeted;
    private final Vector selfVelocity;
    private final int immunityTicks;

    public AbilityContext(Player player, PlayerData playerData, JavaPlugin plugin,
            Location origin, long timestamp,
            DamageAttributionManager damageManager) {
        this(player, playerData, plugin, origin, timestamp, damageManager,
                false, null, 0);
    }

    public AbilityContext(Player player, PlayerData playerData, JavaPlugin plugin,
            Location origin, long timestamp,
            DamageAttributionManager damageManager,
            boolean selfTargeted, Vector selfVelocity, int immunityTicks) {
        this.player = player;
        this.playerData = playerData;
        this.plugin = plugin;
        this.origin = origin;
        this.timestamp = timestamp;
        this.damageManager = damageManager;
        this.selfTargeted = selfTargeted;
        this.selfVelocity = selfVelocity;
        this.immunityTicks = immunityTicks;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public Location getOrigin() {
        return origin;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public DamageAttributionManager getDamageManager() {
        return damageManager;
    }

    // ── Mobility accessors ──

    public boolean isSelfTargeted() {
        return selfTargeted;
    }

    public Vector getSelfVelocity() {
        return selfVelocity;
    }

    public int getImmunityTicks() {
        return immunityTicks;
    }
}
