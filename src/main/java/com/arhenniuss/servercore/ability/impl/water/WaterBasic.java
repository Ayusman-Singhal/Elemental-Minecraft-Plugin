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
 * Water Basic — focused water jet (left-click, empty hand).
 * Visual-only: drip particles along ray + splash sound.
 * Damage + slow applied by AbilityService.
 */
public class WaterBasic implements Ability {

    private final AbilityBalanceConfig config;

    public WaterBasic(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.WATER;
    }

    @Override
    public AbilityType getType() {
        return AbilityType.BASIC;
    }

    @Override
    public CooldownCategory getCooldownCategory() {
        return CooldownCategory.BASIC;
    }

    @Override
    public long getCooldownMillis() {
        return config.getStats(Element.WATER, AbilityType.BASIC).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.WATER, AbilityType.BASIC);
        double reach = stats.reach();
        Vector direction = player.getLocation().getDirection();

        // Water drip particles along the ray
        for (double d = 0.5; d <= reach; d += 0.4) {
            player.getWorld().spawnParticle(
                    Particle.DRIP_WATER,
                    player.getEyeLocation().add(direction.clone().multiply(d)),
                    3, 0.05, 0.05, 0.05, 0);
        }

        // Splash sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 0.8f, 1.2f);
    }
}
