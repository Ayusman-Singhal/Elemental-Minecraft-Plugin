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
import org.bukkit.util.Vector;

/**
 * Water Secondary — tidal push (sneak + left-click, empty hand).
 * Visual-only: wave particles + splash sound.
 * Damage + strong knockback applied by AbilityService.
 */
public class WaterSecondary implements Ability {

    private final AbilityBalanceConfig config;

    public WaterSecondary(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.WATER;
    }

    @Override
    public AbilityType getType() {
        return AbilityType.SECONDARY;
    }

    @Override
    public CooldownCategory getCooldownCategory() {
        return CooldownCategory.SECONDARY;
    }

    @Override
    public long getCooldownMillis() {
        return config.getStats(Element.WATER, AbilityType.SECONDARY).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.WATER, AbilityType.SECONDARY);
        double reach = stats.reach();
        Vector direction = player.getLocation().getDirection();

        // Wave particles along the ray — denser than basic
        for (double d = 0.5; d <= reach; d += 0.3) {
            player.getWorld().spawnParticle(
                    Particle.WATER_SPLASH,
                    player.getEyeLocation().add(direction.clone().multiply(d)),
                    6, 0.1, 0.1, 0.1, 0.1);
        }

        // Sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1.2f, 0.6f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.0f, 0.8f);
    }
}
