package com.arhenniuss.servercore.ability.impl.air;

import com.arhenniuss.servercore.ability.AbilityContext;
import com.arhenniuss.servercore.ability.AbilityType;
import com.arhenniuss.servercore.ability.Ability;
import com.arhenniuss.servercore.ability.CooldownCategory;
import com.arhenniuss.servercore.ability.TargetMode;
import com.arhenniuss.servercore.config.AbilityBalanceConfig;
import com.arhenniuss.servercore.config.AbilityStats;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Cyclone Spin — self-centered AoE that pushes targets outward (left-click,
 * catalyst).
 * Visual: swirling wind ring around caster.
 * Outward push applied by AbilityService.
 */
public class CycloneSpin implements Ability {

    private final AbilityBalanceConfig config;

    public CycloneSpin(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.AIR;
    }

    @Override
    public AbilityType getType() {
        return AbilityType.SPECIAL_SIMPLE;
    }

    @Override
    public CooldownCategory getCooldownCategory() {
        return CooldownCategory.SPECIAL;
    }

    @Override
    public long getCooldownMillis() {
        return config.getStats(Element.AIR, AbilityType.SPECIAL_SIMPLE).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.AOE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.AIR, AbilityType.SPECIAL_SIMPLE);
        double radius = stats.radius();

        // Swirling wind ring
        int points = 32;
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI / points) * i;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            player.getWorld().spawnParticle(
                    Particle.CLOUD,
                    player.getLocation().add(x, 0.6, z),
                    2, 0.05, 0.1, 0.05, 0.03);
            player.getWorld().spawnParticle(
                    Particle.SWEEP_ATTACK,
                    player.getLocation().add(x, 0.8, z),
                    1, 0, 0, 0, 0);
        }

        // Inner vortex
        player.getWorld().spawnParticle(
                Particle.CLOUD,
                player.getLocation().add(0, 0.5, 0),
                10, 0.3, 0.3, 0.3, 0.05);

        // Cyclone sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 0.4f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.6f, 1.2f);
    }
}
