package com.arhenniuss.servercore.combat.reaction.rules;

import com.arhenniuss.servercore.combat.reaction.ReactionContext;
import com.arhenniuss.servercore.combat.reaction.ReactionExecutor;
import com.arhenniuss.servercore.combat.reaction.ReactionRule;
import com.arhenniuss.servercore.combat.status.StatusType;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.Particle;
import org.bukkit.Sound;

/**
 * Fire + BURNING → Flame Amplification
 *
 * Trigger: FIRE ability on a target already BURNING.
 *
 * Effects:
 * 1. Apply bonus damage scaling with remaining burn (base 2.0)
 * 2. Do NOT remove BURNING (non-consuming reaction)
 * 3. Spawn flame burst particles
 *
 * Validates: stacking reactions without status removal.
 */
public class FireIgnitesBurningAmplifyRule implements ReactionRule {

    private static final double BASE_BONUS = 2.0;

    @Override
    public String name() {
        return "Flame Amplification";
    }

    @Override
    public boolean matches(ReactionContext context) {
        return context.elementUsed() == Element.FIRE
                && context.statusesBefore().contains(StatusType.BURNING);
    }

    @Override
    public double execute(ReactionContext context, ReactionExecutor executor) {
        // Scale bonus with base damage (higher base ability = more amplification)
        double bonusDamage = BASE_BONUS + (context.baseDamage() * 0.25);

        // Apply bonus damage via executor — do NOT remove BURNING
        executor.applyReactionDamage(context.target(), bonusDamage);

        // Flame burst particles
        context.target().getWorld().spawnParticle(
                Particle.FLAME,
                context.target().getLocation().add(0, 1, 0),
                30, 0.4, 0.5, 0.4, 0.08);

        context.target().getWorld().spawnParticle(
                Particle.LAVA,
                context.target().getLocation().add(0, 0.5, 0),
                8, 0.3, 0.3, 0.3, 0);

        // Amplification sound
        context.target().getWorld().playSound(
                context.target().getLocation(),
                Sound.ITEM_FIRECHARGE_USE,
                1.0f, 1.2f);

        return bonusDamage;
    }
}
