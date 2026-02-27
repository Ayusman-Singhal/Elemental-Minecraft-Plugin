package com.arhenniuss.servercore.combat.reaction;

/**
 * Defines a modular elemental reaction rule.
 *
 * Rules are registered with ReactionService at startup.
 * Adding new reactions (e.g., Lightning + WET) requires only:
 * 1. A new ReactionRule implementation
 * 2. One registerRule() call
 *
 * Rules must NEVER:
 * - Call target.damage() directly
 * - Call AbilityService.tryExecute()
 * - Modify listeners
 * - Re-trigger ReactionService
 *
 * All damage must go through the provided ReactionExecutor.
 * Status removal MUST happen BEFORE bonus damage to prevent loops.
 */
public interface ReactionRule {

    /** Human-readable reaction name for debug output. */
    String name();

    /** Whether this rule triggers for the given context. */
    boolean matches(ReactionContext context);

    /**
     * Execute the reaction effects.
     * Status removal MUST happen BEFORE bonus damage to prevent reaction loops.
     *
     * @param context  the immutable ability resolution snapshot
     * @param executor the safe damage callback (routes through AbilityService)
     * @return the bonus damage dealt by this reaction (for tracking in
     *         ReactionResult)
     */
    double execute(ReactionContext context, ReactionExecutor executor);
}
