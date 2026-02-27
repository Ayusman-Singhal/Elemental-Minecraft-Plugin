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
 * Fire Special (Charged) — massive fire ring with knockback (sneak+left,
 * catalyst).
 * Visual-only: ring particles + lava burst + sounds. Damage applied by
 * AbilityService.
 */
public class FireSpecialCharged implements Ability {

    private final AbilityBalanceConfig config;

    public FireSpecialCharged(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.FIRE;
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
        return config.getStats(Element.FIRE, AbilityType.SPECIAL_CHARGED).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.AOE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.FIRE, AbilityType.SPECIAL_CHARGED);
        double radius = stats.radius();

        // Large flame ring particle effect
        for (int i = 0; i < 72; i++) {
            double angle = Math.toRadians(i * 5);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            player.getWorld().spawnParticle(
                    Particle.FLAME,
                    player.getLocation().add(x, 0.2, z),
                    3, 0.1, 0.3, 0.1, 0.02);
        }

        // Additional vertical burst at center
        player.getWorld().spawnParticle(
                Particle.LAVA,
                player.getLocation().add(0, 1, 0),
                20, 1.0, 0.5, 1.0, 0);

        // Sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.5f);
    }
}
