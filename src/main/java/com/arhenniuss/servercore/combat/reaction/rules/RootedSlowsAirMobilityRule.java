package com.arhenniuss.servercore.combat.reaction.rules;

import com.arhenniuss.servercore.ability.CooldownCategory;
import com.arhenniuss.servercore.combat.reaction.ReactionContext;
import com.arhenniuss.servercore.combat.reaction.ReactionExecutor;
import com.arhenniuss.servercore.combat.reaction.ReactionRule;
import com.arhenniuss.servercore.combat.status.StatusType;
import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.player.PlayerDataManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Air + ROOTED → Grounded Wing (mobility CD penalty +1s)
 *
 * Trigger: Any ability hits an Air player who has ROOTED.
 * Effect: Extend the target's MOBILITY cooldown by 1000ms.
 * Balance: Earth hard-counters Air's mobility advantage.
 * Only applies to Player targets with AIR element.
 */
public class RootedSlowsAirMobilityRule implements ReactionRule {

    private static final long COOLDOWN_PENALTY_MS = 1000;

    private final PlayerDataManager playerDataManager;
    private final com.arhenniuss.servercore.ability.CooldownManager cooldownManager;

    public RootedSlowsAirMobilityRule(PlayerDataManager playerDataManager,
            com.arhenniuss.servercore.ability.CooldownManager cooldownManager) {
        this.playerDataManager = playerDataManager;
        this.cooldownManager = cooldownManager;
    }

    @Override
    public String name() {
        return "Grounded Wing";
    }

    @Override
    public boolean matches(ReactionContext context) {
        if (!(context.target() instanceof Player targetPlayer))
            return false;
        Element targetElement = playerDataManager.getElement(targetPlayer.getUniqueId());
        return targetElement == Element.AIR
                && context.statusesBefore().contains(StatusType.ROOTED);
    }

    @Override
    public double execute(ReactionContext context, ReactionExecutor executor) {
        Player target = (Player) context.target();

        // Extend mobility cooldown by 1s
        long currentRemaining = cooldownManager.getRemainingMillis(target, CooldownCategory.MOBILITY);
        if (currentRemaining > 0) {
            // Already on cooldown — extend it
            cooldownManager.extendCooldown(target, CooldownCategory.MOBILITY, COOLDOWN_PENALTY_MS);
        }
        // If not on cooldown, no penalty (can't preemptively penalize)

        // Weight chains particles
        target.getWorld().spawnParticle(
                Particle.SMOKE_NORMAL,
                target.getLocation().add(0, 0.5, 0),
                6, 0.2, 0.2, 0.2, 0.02);
        target.getWorld().playSound(
                target.getLocation(),
                Sound.BLOCK_CHAIN_PLACE, 0.6f, 0.8f);

        // No bonus damage — cooldown penalty only
        return 0;
    }
}
