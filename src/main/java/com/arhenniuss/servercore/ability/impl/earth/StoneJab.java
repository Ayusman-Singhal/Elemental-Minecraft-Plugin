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
 * Stone Strike — forward cone melee strike with brief root.
 * Left-click, empty hand.
 * Visual: stone crack particles in a forward cone.
 * Damage, knockback, ROOTED applied by AbilityService.
 */
public class StoneJab implements Ability {

    private final AbilityBalanceConfig config;

    public StoneJab(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.EARTH;
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
        return config.getStats(Element.EARTH, AbilityType.BASIC).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.EARTH, AbilityType.BASIC);
        double reach = stats.reach();
        Vector direction = player.getLocation().getDirection().setY(0).normalize();

        // Forward cone — stone crack particles fanning outward
        for (double d = 0.5; d <= reach; d += 0.4) {
            double spread = d * 0.3; // Cone widens with distance
            for (int i = 0; i < 3; i++) {
                Vector offset = direction.clone().multiply(d)
                        .add(new Vector(
                                (Math.random() - 0.5) * spread,
                                0.2,
                                (Math.random() - 0.5) * spread));
                player.getWorld().spawnParticle(
                        Particle.BLOCK_CRACK,
                        player.getEyeLocation().add(offset),
                        2, 0.05, 0.05, 0.05, 0.01,
                        Material.STONE.createBlockData());
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 0.9f, 1.1f);
    }
}
