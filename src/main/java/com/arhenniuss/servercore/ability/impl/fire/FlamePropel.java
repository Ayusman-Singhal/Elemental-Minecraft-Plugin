package com.arhenniuss.servercore.ability.impl.fire;

import com.arhenniuss.servercore.ability.AbilityContext;
import com.arhenniuss.servercore.ability.AbilityType;
import com.arhenniuss.servercore.ability.Ability;
import com.arhenniuss.servercore.ability.CooldownCategory;
import com.arhenniuss.servercore.ability.TargetMode;
import com.arhenniuss.servercore.config.AbilityBalanceConfig;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Flame Propel — mobility ability with brief immunity (right-click, catalyst).
 * Visual-only: flame trail + explosion burst.
 * Velocity + immunity applied by AbilityService via context-driven branch.
 */
public class FlamePropel implements Ability {

    private final AbilityBalanceConfig config;

    public FlamePropel(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.FIRE;
    }

    @Override
    public AbilityType getType() {
        return AbilityType.MOBILITY;
    }

    @Override
    public CooldownCategory getCooldownCategory() {
        return CooldownCategory.MOBILITY;
    }

    @Override
    public long getCooldownMillis() {
        return config.getStats(Element.FIRE, AbilityType.MOBILITY).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();

        // Flame trail burst
        for (int i = 0; i < 25; i++) {
            player.getWorld().spawnParticle(
                    Particle.FLAME,
                    player.getLocation().add(0, 0.5, 0),
                    3, 0.2, 0.2, 0.2, 0.08);
        }

        // Lava sparks at launch
        player.getWorld().spawnParticle(
                Particle.LAVA,
                player.getLocation(),
                12, 0.3, 0.1, 0.3, 0);

        // Sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.3f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.8f);
    }
}
