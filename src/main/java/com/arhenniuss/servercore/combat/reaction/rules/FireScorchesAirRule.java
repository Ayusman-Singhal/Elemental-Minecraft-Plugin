package com.arhenniuss.servercore.combat.reaction.rules;

import com.arhenniuss.servercore.combat.reaction.ReactionContext;
import com.arhenniuss.servercore.combat.reaction.ReactionExecutor;
import com.arhenniuss.servercore.combat.reaction.ReactionRule;
import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.player.PlayerDataManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Fire vs Air Mobility → Flame Draft (+10% damage)
 *
 * Trigger: FIRE ability hits a Player whose element is AIR.
 * Effect: +10% bonus damage (fire cuts through light frame).
 * Balance: Punishes Air's low durability, rewards Fire aggression.
 */
public class FireScorchesAirRule implements ReactionRule {

    private static final double DAMAGE_BONUS_PERCENT = 0.10;

    private final PlayerDataManager playerDataManager;

    public FireScorchesAirRule(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public String name() {
        return "Flame Draft";
    }

    @Override
    public boolean matches(ReactionContext context) {
        if (context.elementUsed() != Element.FIRE)
            return false;
        if (!(context.target() instanceof Player targetPlayer))
            return false;
        Element targetElement = playerDataManager.getElement(targetPlayer.getUniqueId());
        return targetElement == Element.AIR;
    }

    @Override
    public double execute(ReactionContext context, ReactionExecutor executor) {
        double bonus = context.baseDamage() * DAMAGE_BONUS_PERCENT;
        executor.applyReactionDamage(context.target(), bonus);

        // Flame gust particles
        context.target().getWorld().spawnParticle(
                Particle.FLAME,
                context.target().getLocation().add(0, 1, 0),
                8, 0.3, 0.3, 0.3, 0.04);
        context.target().getWorld().playSound(
                context.target().getLocation(),
                Sound.ITEM_FIRECHARGE_USE, 0.5f, 1.5f);

        return bonus;
    }
}
