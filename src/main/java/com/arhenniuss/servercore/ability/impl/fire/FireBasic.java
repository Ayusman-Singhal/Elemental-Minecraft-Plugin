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
import org.bukkit.util.Vector;

/**
 * Fire Basic — quick flame projectile (left-click, empty hand).
 * Visual-only: particles + sound. Damage applied by AbilityService.
 */
public class FireBasic implements Ability {

    private final AbilityBalanceConfig config;

    public FireBasic(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.FIRE;
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
        return config.getStats(Element.FIRE, AbilityType.BASIC).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.FIRE, AbilityType.BASIC);
        double reach = stats.reach();
        Vector direction = player.getLocation().getDirection();

        // Flame particles along the ray
        for (double d = 0.5; d <= reach; d += 0.5) {
            player.getWorld().spawnParticle(
                    Particle.FLAME,
                    player.getEyeLocation().add(direction.clone().multiply(d)),
                    2, 0.05, 0.05, 0.05, 0.01);
        }

        // Sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
    }
}
