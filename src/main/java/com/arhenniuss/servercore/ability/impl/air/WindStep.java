package com.arhenniuss.servercore.ability.impl.air;

import com.arhenniuss.servercore.ability.AbilityContext;
import com.arhenniuss.servercore.ability.AbilityType;
import com.arhenniuss.servercore.ability.Ability;
import com.arhenniuss.servercore.ability.CooldownCategory;
import com.arhenniuss.servercore.ability.TargetMode;
import com.arhenniuss.servercore.config.AbilityBalanceConfig;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Wind Step — fast forward dash mobility (right-click, empty hand).
 * Visual: cloud trail + sweep effect at launch.
 * Velocity applied by AbilityService via context-driven branch.
 * Fastest mobility in the game — defines Air identity.
 */
public class WindStep implements Ability {

    private final AbilityBalanceConfig config;

    public WindStep(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.AIR;
    }

    @Override
    public AbilityType getType() {
        return AbilityType.MOBILITY;
    }

    @Override
    public CooldownCategory getCooldownCategory() {
        return CooldownCategory.MOBILITY;
    }

    @Override
    public long getCooldownMillis() {
        return config.getStats(Element.AIR, AbilityType.MOBILITY).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();

        // Cloud trail at launch
        player.getWorld().spawnParticle(
                Particle.CLOUD,
                player.getLocation().add(0, 0.5, 0),
                12, 0.3, 0.2, 0.3, 0.04);

        // Sweep effect
        player.getWorld().spawnParticle(
                Particle.SWEEP_ATTACK,
                player.getLocation().add(0, 0.8, 0),
                3, 0.2, 0.1, 0.2, 0);

        // Wind dash sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.7f, 1.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, 0.4f, 1.8f);
    }
}
