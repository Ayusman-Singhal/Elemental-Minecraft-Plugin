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

/**
 * Seismic Slam — self-centered AoE with root + strong knockup.
 * Left-click with catalyst.
 * Visual: stone eruption ring around caster + dust cloud.
 * Damage, ROOTED, and knockup applied by AbilityService.
 */
public class RockSlam implements Ability {

    private final AbilityBalanceConfig config;

    public RockSlam(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.EARTH;
    }

    @Override
    public AbilityType getType() {
        return AbilityType.SPECIAL_SIMPLE;
    }

    @Override
    public CooldownCategory getCooldownCategory() {
        return CooldownCategory.SPECIAL;
    }

    @Override
    public long getCooldownMillis() {
        return config.getStats(Element.EARTH, AbilityType.SPECIAL_SIMPLE).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.AOE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.EARTH, AbilityType.SPECIAL_SIMPLE);
        double radius = stats.radius();

        // Stone eruption ring around caster
        int points = 24;
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI / points) * i;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            player.getWorld().spawnParticle(
                    Particle.BLOCK_CRACK,
                    player.getLocation().add(x, 0.3, z),
                    3, 0.1, 0.3, 0.1, 0.05,
                    Material.STONE.createBlockData());
        }

        // Inner dust burst
        player.getWorld().spawnParticle(
                Particle.CLOUD,
                player.getLocation().add(0, 0.5, 0),
                15, radius * 0.4, 0.3, radius * 0.4, 0.03);

        // Slam sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 0.4f);
    }
}
