package com.arhenniuss.servercore.ability.impl.water;

import com.arhenniuss.servercore.ability.AbilityContext;
import com.arhenniuss.servercore.ability.AbilityType;
import com.arhenniuss.servercore.ability.CooldownCategory;
import com.arhenniuss.servercore.ability.PassiveAbility;
import com.arhenniuss.servercore.ability.TargetMode;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Water Passive — drowning immunity, water breathing, bubble particles.
 * Drowning damage immunity handled by WaterPassiveListener via
 * EntityDamageEvent.
 * onTick grants Water Breathing and spawns ambient bubbles.
 */
public class WaterPassive implements PassiveAbility {

    @Override
    public Element getElement() {
        return Element.WATER;
    }

    @Override
    public AbilityType getType() {
        return AbilityType.PASSIVE;
    }

    @Override
    public CooldownCategory getCooldownCategory() {
        return CooldownCategory.BASIC;
    }

    @Override
    public long getCooldownMillis() {
        return 0;
    }

    @Override
    public TargetMode getTargetMode() {
        return TargetMode.SINGLE;
    }

    @Override
    public void playEffects(AbilityContext context) {
        // Passive — no direct visual effects from execution
    }

    /**
     * Called every 20 ticks by the global passive task.
     * - Grants Water Breathing (prevents drowning overlay)
     * - Spawns occasional bubble particles
     */
    @Override
    public void onTick(AbilityContext context) {
        Player player = context.getPlayer();

        // Keep Water Breathing active (30 ticks = 1.5s, refreshed every second)
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.WATER_BREATHING, 30, 0, true, false, false));

        // Occasional bubble particles (~30% chance per tick cycle)
        if (ThreadLocalRandom.current().nextFloat() < 0.3f) {
            player.getWorld().spawnParticle(
                    Particle.WATER_BUBBLE,
                    player.getLocation().add(0, 0.5, 0),
                    3, 0.2, 0.3, 0.2, 0.01);
        }
    }
}
