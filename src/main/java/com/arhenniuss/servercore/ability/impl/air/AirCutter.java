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
import org.bukkit.util.Vector;

/**
 * Air Cutter — fast forward slash (left-click, empty hand).
 * Visual: sweep particles along ray + whoosh sound.
 * Light damage + brief SLOWED applied by AbilityService.
 */
public class AirCutter implements Ability {

    private final AbilityBalanceConfig config;

    public AirCutter(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.AIR;
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
        return config.getStats(Element.AIR, AbilityType.BASIC).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.AIR, AbilityType.BASIC);
        double reach = stats.reach();
        Vector direction = player.getLocation().getDirection();

        // Sweep particles along the ray
        for (double d = 0.5; d <= reach; d += 0.3) {
            player.getWorld().spawnParticle(
                    Particle.SWEEP_ATTACK,
                    player.getEyeLocation().add(direction.clone().multiply(d)),
                    1, 0.1, 0.1, 0.1, 0);
            player.getWorld().spawnParticle(
                    Particle.CLOUD,
                    player.getEyeLocation().add(direction.clone().multiply(d)),
                    1, 0.05, 0.05, 0.05, 0.01);
        }

        // Whoosh sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.9f, 1.4f);
    }
}
