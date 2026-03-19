package com.arhenniuss.servercore.ability.impl.earth;

import com.arhenniuss.servercore.ability.AbilityContext;
import com.arhenniuss.servercore.ability.AbilityType;
import com.arhenniuss.servercore.ability.Ability;
import com.arhenniuss.servercore.ability.CooldownCategory;
import com.arhenniuss.servercore.ability.TargetMode;
import com.arhenniuss.servercore.config.AbilityBalanceConfig;
import com.arhenniuss.servercore.config.AbilityStats;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Tectonic Break — delayed large self-centered AoE earthquake.
 * Sneak + left-click with catalyst.
 * Visual: two-phase rumble → massive eruption ring.
 * Damage, ROOTED, and strong knockup applied by AbilityService.
 */
public class TectonicBurst implements Ability {

    private final AbilityBalanceConfig config;

    public TectonicBurst(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.EARTH;
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
        return config.getStats(Element.EARTH, AbilityType.SPECIAL_CHARGED).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.AOE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.EARTH, AbilityType.SPECIAL_CHARGED);
        double radius = stats.radius();

        // Phase 1: Rumble warning — smaller ring + tremor sound
        int warningPoints = 24;
        for (int i = 0; i < warningPoints; i++) {
            double angle = (2 * Math.PI / warningPoints) * i;
            double x = Math.cos(angle) * (radius * 0.7);
            double z = Math.sin(angle) * (radius * 0.7);
            player.getWorld().spawnParticle(
                    Particle.BLOCK_CRACK,
                    player.getLocation().add(x, 0.1, z),
                    4, 0.2, 0.1, 0.2, 0.02,
                    Material.STONE.createBlockData());
            player.getWorld().spawnParticle(
                    Particle.SMOKE_NORMAL,
                    player.getLocation().add(x, 0.5, z),
                    2, 0.1, 0.2, 0.1, 0.01);
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.8f, 0.3f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 0.2f);

        // Phase 2: Massive eruption after delay (visual only)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline())
                    return;

                // Full eruption ring with multiple layers
                int fullPoints = 48;
                for (int layer = 0; layer < 3; layer++) {
                    for (int i = 0; i < fullPoints; i++) {
                        double angle = (2 * Math.PI / fullPoints) * i;
                        double x = Math.cos(angle) * (radius - layer * 0.5);
                        double z = Math.sin(angle) * (radius - layer * 0.5);
                        double y = 0.2 + layer * 0.3;
                        
                        player.getWorld().spawnParticle(
                                Particle.BLOCK_CRACK,
                                player.getLocation().add(x, y, z),
                                6, 0.2, 0.8, 0.2, 0.15,
                                Material.COBBLESTONE.createBlockData());
                                
                        player.getWorld().spawnParticle(
                                Particle.EXPLOSION_NORMAL,
                                player.getLocation().add(x, y + 0.5, z),
                                2, 0.3, 0.3, 0.3, 0.05);
                    }
                }

                // Massive dust plume and shockwave
                player.getWorld().spawnParticle(
                        Particle.CLOUD,
                        player.getLocation().add(0, 1, 0),
                        40, radius * 0.6, 1.0, radius * 0.6, 0.1);
                
                // Shockwave ring
                for (int i = 0; i < 36; i++) {
                    double angle = (2 * Math.PI / 36) * i;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    player.getWorld().spawnParticle(
                            Particle.EXPLOSION_NORMAL,
                            player.getLocation().add(x, 0.1, z),
                            3, 0.2, 0.1, 0.2, 0.1);
                }

                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.4f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.2f);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.1f);
            }
        }.runTaskLater(context.getPlugin(), 15); // Increased delay for more anticipation
    }
}
