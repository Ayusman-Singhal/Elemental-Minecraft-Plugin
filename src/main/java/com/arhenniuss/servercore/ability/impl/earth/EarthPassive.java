package com.arhenniuss.servercore.ability.impl.earth;

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
 * Earth Passive — Grounded Body.
 * Knockback reduction handled by EarthPassiveListener via EntityDamageEvent
 * velocity.
 * onTick spawns occasional stone dust particles.
 */
public class EarthPassive implements PassiveAbility {

    @Override
    public Element getElement() {
        return Element.EARTH;
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

        // Occasional stone dust particles (~25% chance per tick cycle)
        if (ThreadLocalRandom.current().nextFloat() < 0.25f) {
            player.getWorld().spawnParticle(
                    Particle.BLOCK_CRACK,
                    player.getLocation().add(0, 0.3, 0),
                    2, 0.2, 0.1, 0.2, 0.01);
        }
    }
}
