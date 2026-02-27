package com.arhenniuss.servercore.ability.impl.water;

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
 * Water Dash — mobility ability (right-click, empty hand).
 * Visual-only: water splash trail.
 * Velocity applied by AbilityService via context-driven branch.
 */
public class WaterDash implements Ability {

    private final AbilityBalanceConfig config;

    public WaterDash(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.WATER;
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
        return config.getStats(Element.WATER, AbilityType.MOBILITY).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();

        // Water splash trail behind player
        for (int i = 0; i < 15; i++) {
            player.getWorld().spawnParticle(
                    Particle.WATER_SPLASH,
                    player.getLocation().add(0, 0.5, 0),
                    5, 0.3, 0.2, 0.3, 0.1);
        }

        player.getWorld().spawnParticle(
                Particle.DOLPHIN,
                player.getLocation().add(0, 0.3, 0),
                10, 0.4, 0.2, 0.4, 0.05);

        // Sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_JUMP, 1.0f, 1.2f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 0.8f, 1.5f);
    }
}
