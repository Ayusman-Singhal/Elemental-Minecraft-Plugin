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
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Skyfall — vertical dash + delayed slam AoE (sneak + left-click, catalyst).
 * Visual: Phase 1 upward cloud burst, Phase 2 downward slam particles.
 * Damage + knockup applied by AbilityService.
 */
public class Skyfall implements Ability {

    private final AbilityBalanceConfig config;

    public Skyfall(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.AIR;
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
        return config.getStats(Element.AIR, AbilityType.SPECIAL_CHARGED).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.AOE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.AIR, AbilityType.SPECIAL_CHARGED);
        double radius = stats.radius();

        // Phase 1: Upward launch burst
        player.getWorld().spawnParticle(
                Particle.CLOUD,
                player.getLocation().add(0, 1, 0),
                20, 0.3, 0.5, 0.3, 0.08);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.9f, 0.5f);

        // Phase 2: Slam impact after delay (visual only)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline())
                    return;

                // Shockwave ring
                int points = 24;
                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI / points) * i;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    player.getWorld().spawnParticle(
                            Particle.CLOUD,
                            player.getLocation().add(x, 0.3, z),
                            3, 0.1, 0.2, 0.1, 0.05);
                    player.getWorld().spawnParticle(
                            Particle.SWEEP_ATTACK,
                            player.getLocation().add(x, 0.5, z),
                            1, 0, 0, 0, 0);
                }

                // Ground impact dust
                player.getWorld().spawnParticle(
                        Particle.EXPLOSION_LARGE,
                        player.getLocation(),
                        3, 0.5, 0.1, 0.5, 0);

                // Impact sounds
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 0.8f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 0.5f);
            }
        }.runTaskLater(context.getPlugin(), 8); // 8-tick delay per spec
    }
}
