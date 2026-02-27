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
 * Water + ROOTED → Mudlock (extend ROOTED by 10 ticks)
 *
 * Trigger: WATER ability hits a target that has ROOTED.
 * Effect: Extend ROOTED duration by 10 ticks (mud hardens).
 * Balance: Water + Earth synergy for extended lockdown.
 */
public class WaterExtendsRootRule implements ReactionRule {

    private static final int EXTENSION_TICKS = 10;

    private final StatusService statusService;

    public WaterExtendsRootRule(StatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    public String name() {
        return "Mudlock";
    }

    @Override
    public boolean matches(ReactionContext context) {
        return context.elementUsed() == Element.WATER
                && context.statusesBefore().contains(StatusType.ROOTED);
    }

    @Override
    public double execute(ReactionContext context, ReactionExecutor executor) {
        // Extend ROOTED by re-applying with additional ticks
        statusService.applyStatus(context.target(), StatusType.ROOTED, EXTENSION_TICKS);

        // Mud particles
        context.target().getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                context.target().getLocation().add(0, 0.2, 0),
                8, 0.3, 0.1, 0.3, 0.02,
                org.bukkit.Material.MUD.createBlockData());
        context.target().getWorld().playSound(
                context.target().getLocation(),
                Sound.BLOCK_MUD_STEP, 0.8f, 0.6f);

        // No bonus damage — pure status extension
        return 0;
    }
}
