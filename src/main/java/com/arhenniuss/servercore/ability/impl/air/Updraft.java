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
import org.bukkit.util.Vector;

/**
 * Updraft — forward cone gust that lifts targets (sneak + left-click, empty
 * hand).
 * Visual: upward cloud burst in a cone.
 * Knockup applied by AbilityService.
 */
public class Updraft implements Ability {

    private final AbilityBalanceConfig config;

    public Updraft(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.AIR;
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
        return config.getStats(Element.AIR, AbilityType.SECONDARY).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();
        AbilityStats stats = config.getStats(Element.AIR, AbilityType.SECONDARY);
        double reach = stats.reach();
        Vector direction = player.getLocation().getDirection().setY(0).normalize();

        // Upward gust cone
        for (double d = 0.5; d <= reach; d += 0.4) {
            double spread = d * 0.35;
            for (int i = 0; i < 3; i++) {
                Vector offset = direction.clone().multiply(d)
                        .add(new Vector(
                                (Math.random() - 0.5) * spread,
                                0.3 + d * 0.15,
                                (Math.random() - 0.5) * spread));
                player.getWorld().spawnParticle(
                        Particle.CLOUD,
                        player.getLocation().add(offset),
                        2, 0.05, 0.15, 0.05, 0.02);
            }
        }

        // Wind gust sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.8f, 0.6f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, 0.5f, 1.5f);
    }
}
