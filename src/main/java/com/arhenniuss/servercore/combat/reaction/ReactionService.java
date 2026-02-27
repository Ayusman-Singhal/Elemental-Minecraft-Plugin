package com.arhenniuss.servercore.combat.reaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates registered reaction rules after ability resolution.
 *
 * Processing rules:
 * - ALL matching rules execute — no short-circuit
 * - Rules execute in registration order (deterministic)
 * - ReactionResult accumulates all triggered names + total bonus damage
 *
 * Adding a new element interaction requires only:
 * 1. Implement ReactionRule
 * 2. Call registerRule() during startup
 * No modifications to this class needed.
 */
public class ReactionService {

    private final List<ReactionRule> rules = new ArrayList<>();

    /**
     * Registers a reaction rule. Called during plugin startup by element modules.
     */
    public void registerRule(ReactionRule rule) {
        rules.add(rule);
    }

    /**
     * Evaluates all registered rules against the context.
     * All matching rules execute in registration order.
     *
     * @param context  the immutable ability resolution snapshot
     * @param executor the safe damage callback provided by AbilityService
     * @return accumulated result of all triggered reactions
     */
    public ReactionResult process(ReactionContext context, ReactionExecutor executor) {
        ReactionResult result = new ReactionResult();

        for (ReactionRule rule : rules) {
            if (rule.matches(context)) {
                double bonusDamage = rule.execute(context, executor);
                result.addReaction(rule.name(), bonusDamage);
            }
        }

        return result;
    }
}
