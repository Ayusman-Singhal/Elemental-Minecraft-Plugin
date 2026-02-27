package com.arhenniuss.servercore.combat.reaction;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Safe damage callback provided by AbilityService to reaction rules.
 *
 * This is the ONLY way reaction rules may apply damage.
 * Implementations route through AbilityService's internal methods,
 * preserving centralized combat authority and enabling future
 * hook points (shields, lifesteal, damage modifiers, analytics).
 *
 * Rules must NEVER call target.damage() directly.
 */
public interface ReactionExecutor {

    /**
     * Applies reaction bonus damage to a single target.
     * Routed through AbilityService.applyReactionDamageInternal().
     */
    void applyReactionDamage(LivingEntity target, double damage);

    /**
     * Applies reaction AoE damage around the caster.
     * Routed through AbilityService.applyReactionAoEInternal().
     */
    void applyReactionAoE(Player caster, double radius, double damage);
}
