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

        // Multi-layer swirling wind ring for more intensity
        for (int layer = 0; layer < 3; layer++) {
            int points = 40;
            for (int i = 0; i < points; i++) {
                double angle = (2 * Math.PI / points) * i;
                double x = Math.cos(angle) * (radius - layer * 0.3);
                double z = Math.sin(angle) * (radius - layer * 0.3);
                double y = 0.5 + layer * 0.3;
                
                player.getWorld().spawnParticle(
                        Particle.CLOUD,
                        player.getLocation().add(x, y, z),
                        4, 0.1, 0.2, 0.1, 0.05);
                player.getWorld().spawnParticle(
                        Particle.SWEEP_ATTACK,
                        player.getLocation().add(x, y + 0.2, z),
                        2, 0.1, 0.1, 0.1, 0.02);
            }
        }

        // Vertical vortex column
        for (double y = 0.2; y <= 2.5; y += 0.3) {
            player.getWorld().spawnParticle(
                    Particle.CLOUD,
                    player.getLocation().add(0, y, 0),
                    15, 0.5, 0.1, 0.5, 0.08);
        }

        // Ground-level swirl
        for (int i = 0; i < 24; i++) {
            double angle = (2 * Math.PI / 24) * i;
            double x = Math.cos(angle) * (radius * 0.5);
            double z = Math.sin(angle) * (radius * 0.5);
            player.getWorld().spawnParticle(
                    Particle.CLOUD,
                    player.getLocation().add(x, 0.1, z),
                    3, 0.1, 0.05, 0.1, 0.03);
        }

        // Powerful cyclone sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.5f, 0.3f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.8f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_WIND_BURST, 1.2f, 1.0f);
    }
}
