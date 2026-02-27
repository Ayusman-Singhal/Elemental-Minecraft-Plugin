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
 * Fire Secondary — powerful burst (sneak + left-click, empty hand).
 * Visual-only: burst particles + sounds. Damage applied by AbilityService.
 */
public class FireSecondary implements Ability {

    private final AbilityBalanceConfig config;

    public FireSecondary(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.FIRE;
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
        return config.getStats(Element.FIRE, AbilityType.SECONDARY).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.FIRE, AbilityType.SECONDARY);
        double reach = stats.reach();
        Vector direction = player.getLocation().getDirection();

        // Fire burst particles along the ray
        for (double d = 0.5; d <= reach; d += 0.4) {
            player.getWorld().spawnParticle(
                    Particle.FLAME,
                    player.getEyeLocation().add(direction.clone().multiply(d)),
                    4, 0.1, 0.1, 0.1, 0.02);
        }

        // Sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.2f, 0.7f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.2f);
    }
}
