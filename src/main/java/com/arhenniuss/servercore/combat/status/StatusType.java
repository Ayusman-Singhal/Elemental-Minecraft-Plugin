package com.arhenniuss.servercore.combat.status;

import org.bukkit.potion.PotionEffectType;

/**
 * Conceptual elemental statuses applied by abilities.
 * Each status has a corresponding Bukkit implementation
 * that StatusService uses internally.
 *
 * Phase 6 will use these for reaction checks
 * (e.g. WET + BURNING → Steam).
 */
public enum StatusType {

    /** Applied by Water abilities. Causes Slowness I. */
    WET(PotionEffectType.SLOW, 0, false),

    /** Applied by Fire abilities. Sets entity on fire. */
    BURNING(null, 0, true),

    /** Future: prevents movement and actions. */
    STUNNED(PotionEffectType.SLOW, 4, false),

    /** Future: prevents movement, allows actions. */
    ROOTED(PotionEffectType.SLOW, 127, false),

    /** Future: generic slow. */
    SLOWED(PotionEffectType.SLOW, 1, false);

    private final PotionEffectType potionEffect;
    private final int amplifier;
    private final boolean usesFireTicks;

    StatusType(PotionEffectType potionEffect, int amplifier, boolean usesFireTicks) {
        this.potionEffect = potionEffect;
        this.amplifier = amplifier;
        this.usesFireTicks = usesFireTicks;
    }

    /**
     * The Bukkit PotionEffectType to apply, or null if this status uses fire ticks.
     */
    public PotionEffectType getPotionEffect() {
        return potionEffect;
    }

    /** The amplifier level for the potion effect (0-indexed). */
    public int getAmplifier() {
        return amplifier;
    }

    /**
     * Whether this status is implemented via fire ticks instead of potion effects.
     */
    public boolean usesFireTicks() {
        return usesFireTicks;
    }
}
