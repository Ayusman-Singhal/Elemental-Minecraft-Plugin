package com.arhenniuss.servercore.combat.reaction.rules;

import com.arhenniuss.servercore.combat.reaction.ReactionContext;
import com.arhenniuss.servercore.combat.reaction.ReactionExecutor;
import com.arhenniuss.servercore.combat.reaction.ReactionRule;
import com.arhenniuss.servercore.combat.status.StatusService;
import com.arhenniuss.servercore.combat.status.StatusType;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

/**
 * Earth + WET → Mud Harden (Root)
 *
 * Trigger: EARTH ability on a WET target.
 *
 * Effects (in order):
 * 1. Remove WET (BEFORE status application — prevents loops)
 * 2. Apply ROOTED via StatusService (60 ticks = 3 seconds)
 * 3. Spawn block crack particles (dirt)
 * 4. No bonus damage
 *
 * Validates: control-only reactions with zero damage.
 */
public class EarthHardensWetRule implements ReactionRule {

    private static final int ROOT_DURATION_TICKS = 60; // 3 seconds

    private final StatusService statusService;

    public EarthHardensWetRule(StatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    public String name() {
        return "Mud Harden";
    }

    @Override
    public boolean matches(ReactionContext context) {
        return context.elementUsed() == Element.EARTH
                && context.statusesBefore().contains(StatusType.WET);
    }

    @Override
    public double execute(ReactionContext context, ReactionExecutor executor) {
        // 1. Remove WET FIRST
        statusService.removeStatus(context.target(), StatusType.WET);

        // 2. Apply ROOTED via StatusService
        statusService.applyStatus(context.target(), StatusType.ROOTED, ROOT_DURATION_TICKS);

        // 3. Block crack particles (dirt/mud)
        context.target().getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                context.target().getLocation().add(0, 0.5, 0),
                30, 0.3, 0.3, 0.3, 0.1,
                Material.DIRT.createBlockData());

        // 4. Hardening sound
        context.target().getWorld().playSound(
                context.target().getLocation(),
                Sound.BLOCK_GRAVEL_BREAK,
                1.0f, 0.6f);

        // No bonus damage — pure control effect
        return 0;
    }
}
