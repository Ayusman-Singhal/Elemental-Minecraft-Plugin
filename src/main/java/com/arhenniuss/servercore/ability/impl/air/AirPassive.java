package com.arhenniuss.servercore.ability.impl.air;

import com.arhenniuss.servercore.ability.AbilityContext;
import com.arhenniuss.servercore.ability.AbilityType;
import com.arhenniuss.servercore.ability.CooldownCategory;
import com.arhenniuss.servercore.ability.PassiveAbility;
import com.arhenniuss.servercore.ability.TargetMode;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Air Passive — Light Frame.
 * +5% movement speed handled by AirPassiveListener via attribute modifier.
 * 20% fall damage reduction handled by AirPassiveListener.
 * onTick spawns occasional wind particles.
 */
public class AirPassive implements PassiveAbility {

    @Override
    public Element getElement() {
        return Element.AIR;
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

    @Override
    public void onTick(AbilityContext context) {
        Player player = context.getPlayer();

        // Occasional wind particles (~20% chance per tick cycle)
        if (ThreadLocalRandom.current().nextFloat() < 0.20f) {
            player.getWorld().spawnParticle(
                    Particle.CLOUD,
                    player.getLocation().add(
                            (Math.random() - 0.5) * 0.6,
                            0.5 + Math.random() * 0.5,
                            (Math.random() - 0.5) * 0.6),
                    1, 0, 0.05, 0, 0.01);
        }
    }
}
