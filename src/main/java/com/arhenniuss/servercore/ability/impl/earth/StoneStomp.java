package com.arhenniuss.servercore.ability.impl.earth;

import com.arhenniuss.servercore.ability.AbilityContext;
import com.arhenniuss.servercore.ability.AbilityType;
import com.arhenniuss.servercore.ability.Ability;
import com.arhenniuss.servercore.ability.CooldownCategory;
import com.arhenniuss.servercore.ability.TargetMode;
import com.arhenniuss.servercore.config.AbilityBalanceConfig;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Stone Surge — forward dash mobility ability.
 * Right-click, empty hand.
 * Visual: stone trail behind player + ground crack at launch.
 * Velocity applied by AbilityService via context-driven branch.
 * Clears ROOTED on Earth (Phase 7.5 self-cleanse).
 */
public class StoneStomp implements Ability {

    private final AbilityBalanceConfig config;

    public StoneStomp(AbilityBalanceConfig config) {
        this.config = config;
    }

    @Override
    public Element getElement() {
        return Element.EARTH;
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
        return config.getStats(Element.EARTH, AbilityType.MOBILITY).cooldownMs();
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        Player player = context.getPlayer();

        // Ground crack at launch point
        player.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                player.getLocation(),
                25, 0.4, 0.1, 0.4, 0.08,
                Material.STONE.createBlockData());

        // Forward trail particles
        for (int i = 0; i < 10; i++) {
            player.getWorld().spawnParticle(
                    Particle.BLOCK_CRACK,
                    player.getLocation().add(0, 0.4, 0),
                    2, 0.15, 0.1, 0.15, 0.03,
                    Material.COBBLESTONE.createBlockData());
        }

        // Dust burst
        player.getWorld().spawnParticle(
                Particle.CLOUD,
                player.getLocation().add(0, 0.3, 0),
                6, 0.3, 0.1, 0.3, 0.02);

        // Sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 0.7f, 0.8f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 0.9f, 0.7f);
    }
}
