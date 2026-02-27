package com.arhenniuss.servercore.combat.reaction.rules;

import com.arhenniuss.servercore.combat.reaction.ReactionContext;
import com.arhenniuss.servercore.combat.reaction.ReactionExecutor;
import com.arhenniuss.servercore.combat.reaction.ReactionRule;
import com.arhenniuss.servercore.combat.status.StatusType;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;

/**
 * Fire vs WET → Dampened Flame (-15% damage)
 *
 * Trigger: FIRE ability hits a target that has WET status.
 * Effect: Reduce effective damage by 15% (applied as negative bonus).
 * Balance: WET acts as a defensive counter to Fire burst.
 *
 * Note: This is distinct from FireEvaporatesWetRule which removes WET and
 * adds bonus damage. This rule fires in parallel — the net effect is:
 * Steam Evaporation (+3.0 bonus) dampened by Wet Shield (-15% base).
 * Both rules trigger independently per the reaction engine.
 */
public class WetDampensFireRule implements ReactionRule {

    private static final double DAMAGE_REDUCTION_PERCENT = 0.15;

    @Override
    public String name() {
        return "Dampened Flame";
    }

    @Override
    public boolean matches(ReactionContext context) {
        return context.elementUsed() == Element.FIRE
                && context.statusesBefore().contains(StatusType.WET);
    }

    @Override
    public double execute(ReactionContext context, ReactionExecutor executor) {
        // Heal back a portion of the base damage to simulate reduction
        // (damage already applied — we compensate via negative reaction damage)
        double reduction = context.baseDamage() * DAMAGE_REDUCTION_PERCENT;

        // Steam shield particles (blue-ish cloud)
        context.target().getWorld().spawnParticle(
                Particle.DRIP_WATER,
                context.target().getLocation().add(0, 1, 0),
                10, 0.3, 0.4, 0.3, 0);
        context.target().getWorld().playSound(
                context.target().getLocation(),
                Sound.ENTITY_GENERIC_SPLASH, 0.4f, 1.8f);

        // Negative bonus: the target receives health back
        // Since we can't un-damage, we return 0 and use the entity heal API
        context.target().setHealth(
                Math.min(context.target().getHealth() + reduction,
                        context.target().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));

        // Return 0 — not bonus damage, it's a reduction
        return 0;
    }
}
