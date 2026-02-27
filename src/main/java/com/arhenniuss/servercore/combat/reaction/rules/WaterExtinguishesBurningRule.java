package com.arhenniuss.servercore.combat.reaction.rules;

import com.arhenniuss.servercore.combat.reaction.ReactionContext;
import com.arhenniuss.servercore.combat.reaction.ReactionExecutor;
import com.arhenniuss.servercore.combat.reaction.ReactionRule;
import com.arhenniuss.servercore.combat.status.StatusService;
import com.arhenniuss.servercore.combat.status.StatusType;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.Particle;
import org.bukkit.Sound;

/**
 * Water + BURNING → Extinguish
 *
 * Trigger: WATER ability on a target that is BURNING.
 *
 * Effects (in order):
 * 1. Remove BURNING (BEFORE damage — prevents reaction loops)
 * 2. Apply 2.5 bonus steam burst damage via executor
 * 3. Spawn cloud/steam particles
 *
 * Validates: cross-element status removal using only existing elements.
 */
public class WaterExtinguishesBurningRule implements ReactionRule {

    private static final double BONUS_DAMAGE = 2.5;

    private final StatusService statusService;

    public WaterExtinguishesBurningRule(StatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    public String name() {
        return "Extinguish";
    }

    @Override
    public boolean matches(ReactionContext context) {
        return context.elementUsed() == Element.WATER
                && context.statusesBefore().contains(StatusType.BURNING);
    }

    @Override
    public double execute(ReactionContext context, ReactionExecutor executor) {
        // 1. Remove BURNING FIRST
        statusService.removeStatus(context.target(), StatusType.BURNING);

        // 2. Steam burst bonus damage
        executor.applyReactionDamage(context.target(), BONUS_DAMAGE);

        // 3. Steam particles
        context.target().getWorld().spawnParticle(
                Particle.CLOUD,
                context.target().getLocation().add(0, 1, 0),
                15, 0.4, 0.5, 0.4, 0.05);

        // 4. Extinguish hiss
        context.target().getWorld().playSound(
                context.target().getLocation(),
                Sound.BLOCK_FIRE_EXTINGUISH,
                1.0f, 1.2f);

        return BONUS_DAMAGE;
    }
}
