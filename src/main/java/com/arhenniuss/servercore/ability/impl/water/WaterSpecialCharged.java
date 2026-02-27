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
 * Water Special (Charged) — Maelstrom (sneak+left, catalyst).
 * Visual-only: vortex particles + thunder sound.
 * Two-phase displacement (pull + push) handled by AbilityService.
 */
public class WaterSpecialCharged implements Ability {

    private final AbilityBalanceConfig config;

    public WaterSpecialCharged(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.WATER;
    }

    @Override
    public AbilityType getType() {
        return AbilityType.SPECIAL_CHARGED;
    }

    @Override
    public CooldownCategory getCooldownCategory() {
        return CooldownCategory.SPECIAL;
    }

    @Override
    public long getCooldownMillis() {
        return config.getStats(Element.WATER, AbilityType.SPECIAL_CHARGED).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.AOE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.WATER, AbilityType.SPECIAL_CHARGED);
        double radius = stats.radius();

        // Vortex spiral particles
        for (int ring = 0; ring < 3; ring++) {
            double r = radius * (ring + 1) / 3.0;
            for (int i = 0; i < 36; i++) {
                double angle = Math.toRadians(i * 10 + ring * 30);
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                double y = 0.2 + ring * 0.4;
                player.getWorld().spawnParticle(
                        Particle.WATER_SPLASH,
                        player.getLocation().add(x, y, z),
                        3, 0.05, 0.1, 0.05, 0.05);
            }
        }

        // Central water column
        for (double y = 0; y < 3.0; y += 0.3) {
            player.getWorld().spawnParticle(
                    Particle.DRIP_WATER,
                    player.getLocation().add(0, y, 0),
                    5, 0.3, 0.1, 0.3, 0);
        }

        // Sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.0f, 0.5f);
    }
}
