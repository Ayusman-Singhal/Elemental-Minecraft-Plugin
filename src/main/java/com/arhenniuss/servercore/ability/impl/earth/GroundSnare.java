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
import org.bukkit.util.Vector;

/**
 * Fault Line — linear forward ground crack with slow + minor knockup.
 * Sneak + left-click, empty hand.
 * Visual: dirt crack particles in a forward line.
 * Damage, SLOWED, and knockup applied by AbilityService.
 */
public class GroundSnare implements Ability {

    private final AbilityBalanceConfig config;

    public GroundSnare(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.EARTH;
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
        return config.getStats(Element.EARTH, AbilityType.SECONDARY).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.EARTH, AbilityType.SECONDARY);
        double reach = stats.reach();
        Vector direction = player.getLocation().getDirection().setY(0).normalize();

        // Linear forward ground crack
        for (double d = 0.5; d <= reach; d += 0.3) {
            Vector pos = direction.clone().multiply(d);
            player.getWorld().spawnParticle(
                    Particle.BLOCK_CRACK,
                    player.getLocation().add(pos).add(0, 0.15, 0),
                    4, 0.15, 0.05, 0.15, 0.02,
                    Material.DIRT.createBlockData());
            // Small upward dust at each point
            player.getWorld().spawnParticle(
                    Particle.CLOUD,
                    player.getLocation().add(pos).add(0, 0.3, 0),
                    1, 0.05, 0.1, 0.05, 0.01);
        }

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.6f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RAVAGER_STEP, 0.7f, 0.5f);
    }
}
