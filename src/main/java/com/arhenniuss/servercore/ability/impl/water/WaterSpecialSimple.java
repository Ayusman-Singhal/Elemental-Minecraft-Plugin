package com.arhenniuss.servercore.ability.impl.water;

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
 * Water Special (Simple) — Aqua Ring (left-click, catalyst).
 * Visual-only: water ring particles + trident sound.
 * AOE damage + knockback applied by AbilityService.
 */
public class WaterSpecialSimple implements Ability {

    private final AbilityBalanceConfig config;

    public WaterSpecialSimple(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.WATER;
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
        return config.getStats(Element.WATER, AbilityType.SPECIAL_SIMPLE).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.AOE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.WATER, AbilityType.SPECIAL_SIMPLE);
        double radius = stats.radius();

        // Water ring particles
        for (int i = 0; i < 60; i++) {
            double angle = Math.toRadians(i * 6);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            player.getWorld().spawnParticle(
                    Particle.DRIP_WATER,
                    player.getLocation().add(x, 0.5, z),
                    2, 0.1, 0.2, 0.1, 0);
        }

        // Additional splash at center
        player.getWorld().spawnParticle(
                Particle.WATER_SPLASH,
                player.getLocation().add(0, 0.5, 0),
                15, 0.5, 0.3, 0.5, 0.1);

        // Sounds
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.2f, 0.8f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1.0f, 0.6f);
    }
}
