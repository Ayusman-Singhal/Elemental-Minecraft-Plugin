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
 * Fire + WET → Steam Evaporation
 *
 * Trigger: FIRE ability used on a target that has StatusType.WET
 * (checked via statusesBefore snapshot — captured BEFORE ability statuses)
 *
 * Effects (in order):
 * 1. Remove WET via StatusService (BEFORE damage — prevents reaction loops)
 * 2. Apply bonus direct damage via executor (3.0)
 * 3. Spawn steam particles
 * 4. Optional small AoE splash via executor (radius 2.5, damage 1.5)
 *
 * Does NOT:
 * - Call target.damage() directly
 * - Call AbilityService.tryExecute()
 * - Re-trigger ReactionService
 * - Touch listeners
 */
public class FireEvaporatesWetRule implements ReactionRule {

    private static final double BONUS_DAMAGE = 3.0;
    private static final double AOE_RADIUS = 2.5;
    private static final double AOE_DAMAGE = 1.5;

    private final StatusService statusService;

    public FireEvaporatesWetRule(StatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    public String name() {
        return "Steam Evaporation";
    }

    @Override
    public boolean matches(ReactionContext context) {
        return context.elementUsed() == Element.FIRE
                && context.statusesBefore().contains(StatusType.WET);
    }

    @Override
    public double execute(ReactionContext context, ReactionExecutor executor) {
        // 1. Remove WET FIRST (prevents reaction loops)
        statusService.removeStatus(context.target(), StatusType.WET);

        // 2. Apply bonus damage via executor (routed through AbilityService)
        executor.applyReactionDamage(context.target(), BONUS_DAMAGE);

        // 3. Steam particles
        context.target().getWorld().spawnParticle(
                Particle.CLOUD,
                context.target().getLocation().add(0, 1, 0),
                20, 0.5, 0.5, 0.5, 0.05);

        // 4. Steam hiss sound
        context.target().getWorld().playSound(
                context.target().getLocation(),
                Sound.BLOCK_FIRE_EXTINGUISH,
                1.2f, 1.5f);

        // 5. Optional small AoE splash (steam burst)
        executor.applyReactionAoE(context.caster(), AOE_RADIUS, AOE_DAMAGE);

        return BONUS_DAMAGE;
    }
}
