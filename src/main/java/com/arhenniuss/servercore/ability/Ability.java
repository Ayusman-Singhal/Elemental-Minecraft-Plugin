package com.arhenniuss.servercore.ability;

import com.arhenniuss.servercore.element.Element;

/**
 * Ability interface — abilities define their metadata and visual effects only.
 * All damage, knockback, targeting, and cooldown is handled by AbilityService.
 */
public interface Ability {

    Element getElement();

    AbilityType getType();

    CooldownCategory getCooldownCategory();

    long getCooldownMillis();

    /**
     * Returns the targeting mode for this ability.
     * SINGLE = ray trace for one target, AOE = radius-based multi-target.
     */
    TargetMode getTargetMode();

    /**
     * Play visual effects (particles + sounds) for this ability.
     * Must NOT deal damage, apply knockback, or modify entity state.
     * AbilityService handles all combat logic.
     */
    void playEffects(AbilityContext context);
}
