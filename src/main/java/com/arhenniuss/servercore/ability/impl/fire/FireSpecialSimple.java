package com.arhenniuss.servercore.ability.impl.fire;

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
 * Fire Special (Simple) — AoE burst centered on player (left-click, catalyst).
 * Visual-only: sphere particles + explosion sound. Damage applied by
 * AbilityService.
 */
public class FireSpecialSimple implements Ability {

    private final AbilityBalanceConfig config;

    public FireSpecialSimple(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.FIRE;
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
        return config.getStats(Element.FIRE, AbilityType.SPECIAL_SIMPLE).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.AOE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.FIRE, AbilityType.SPECIAL_SIMPLE);
        double radius = stats.radius();

        // Flame burst particles in a sphere
        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * radius;
            double x = Math.cos(angle) * r;
            double z = Math.sin(angle) * r;
            player.getWorld().spawnParticle(
                    Particle.FLAME,
                    player.getLocation().add(x, 0.5, z),
                    1, 0, 0.2, 0, 0.05);
        }

        // Sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
    }
}
