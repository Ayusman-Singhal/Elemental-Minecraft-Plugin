package com.arhenniuss.servercore.combat.reaction.rules;

import com.arhenniuss.servercore.ability.AbilityType;
import com.arhenniuss.servercore.combat.reaction.ReactionContext;
import com.arhenniuss.servercore.combat.reaction.ReactionExecutor;
import com.arhenniuss.servercore.combat.reaction.ReactionRule;
import com.arhenniuss.servercore.combat.status.StatusService;
import com.arhenniuss.servercore.combat.status.StatusType;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.Particle;
import org.bukkit.Sound;

/**
 * Updraft (Air SECONDARY) vs ROOTED → Gale Break (remove ROOTED)
 *
 * Trigger: AIR SECONDARY hits a target that has ROOTED.
 * Effect: Remove ROOTED (Air gust breaks earth's grip).
 * Balance: Air can liberate allies or self-counter Earth's control.
 * No bonus damage — pure utility reaction.
 */
public class AirBreaksRootRule implements ReactionRule {

    private final StatusService statusService;

    public AirBreaksRootRule(StatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    public String name() {
        return "Gale Break";
    }

    @Override
    public boolean matches(ReactionContext context) {
        return context.elementUsed() == Element.AIR
                && context.abilityType() == AbilityType.SECONDARY
                && context.statusesBefore().contains(StatusType.ROOTED);
    }

    @Override
    public double execute(ReactionContext context, ReactionExecutor executor) {
        // Remove ROOTED (wind tears roots free)
        statusService.removeStatus(context.target(), StatusType.ROOTED);

        // Wind burst particles
        context.target().getWorld().spawnParticle(
                Particle.CLOUD,
                context.target().getLocation().add(0, 0.5, 0),
                12, 0.4, 0.3, 0.4, 0.06);
        context.target().getWorld().spawnParticle(
                Particle.SWEEP_ATTACK,
                context.target().getLocation().add(0, 0.8, 0),
                3, 0.2, 0.1, 0.2, 0);

        // Breaking free sound
        context.target().getWorld().playSound(
                context.target().getLocation(),
                Sound.ENTITY_PHANTOM_FLAP, 0.8f, 1.2f);

        // No bonus damage — utility reaction
        return 0;
    }
}
