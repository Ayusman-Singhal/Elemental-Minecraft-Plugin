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
        int warningPoints = 16;
        for (int i = 0; i < warningPoints; i++) {
            double angle = (2 * Math.PI / warningPoints) * i;
            double x = Math.cos(angle) * (radius * 0.5);
            double z = Math.sin(angle) * (radius * 0.5);
            player.getWorld().spawnParticle(
                    Particle.BLOCK_CRACK,
                    player.getLocation().add(x, 0.1, z),
                    2, 0.1, 0.05, 0.1, 0.01,
                    Material.STONE.createBlockData());
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.6f, 0.3f);

        // Phase 2: Massive eruption after delay (visual only)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline())
                    return;

                // Full eruption ring
                int fullPoints = 32;
                for (int i = 0; i < fullPoints; i++) {
                    double angle = (2 * Math.PI / fullPoints) * i;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    player.getWorld().spawnParticle(
                            Particle.BLOCK_CRACK,
                            player.getLocation().add(x, 0.5, z),
                            4, 0.1, 0.5, 0.1, 0.12,
                            Material.COBBLESTONE.createBlockData());
                }

                // Massive dust plume
                player.getWorld().spawnParticle(
                        Particle.CLOUD,
                        player.getLocation().add(0, 1, 0),
                        25, radius * 0.4, 0.6, radius * 0.4, 0.06);

                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.4f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.4f, 0.2f);
            }
        }.runTaskLater(context.getPlugin(), 10); // 10-tick delay per spec
    }
}
