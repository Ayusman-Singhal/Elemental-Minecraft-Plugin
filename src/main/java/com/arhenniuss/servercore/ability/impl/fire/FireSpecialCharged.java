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

        // Large flame ring particle effect with multiple layers
        for (int layer = 0; layer < 3; layer++) {
            for (int i = 0; i < 72; i++) {
                double angle = Math.toRadians(i * 5);
                double x = Math.cos(angle) * (radius - layer * 0.5);
                double z = Math.sin(angle) * (radius - layer * 0.5);
                double y = 0.2 + layer * 0.3;
                player.getWorld().spawnParticle(
                        Particle.FLAME,
                        player.getLocation().add(x, y, z),
                        5, 0.2, 0.5, 0.2, 0.05);
            }
        }

        // Additional vertical burst at center with explosion effect
        player.getWorld().spawnParticle(
                Particle.LAVA,
                player.getLocation().add(0, 1, 0),
                30, 1.5, 1.0, 1.5, 0.1);
        
        // Explosion particles
        player.getWorld().spawnParticle(
                Particle.EXPLOSION_HUGE,
                player.getLocation().add(0, 1, 0),
                3, 1.0, 1.0, 1.0, 0);

        // Multiple sounds for more impact
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.3f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.1f);
        
        // Screen shake effect (visual cue)
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.getLocation().distance(player.getLocation()) <= radius * 2) {
                // This would require a client-side plugin or packet manipulation
                // For now, we'll just add more visual cues
                nearby.getWorld().spawnParticle(
                        Particle.EXPLOSION_NORMAL,
                        nearby.getLocation(),
                        5, 0.1, 0.1, 0.1, 0.1);
            }
        }
    }
}
