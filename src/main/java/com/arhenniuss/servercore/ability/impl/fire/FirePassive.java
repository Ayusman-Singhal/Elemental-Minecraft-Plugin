package com.arhenniuss.servercore.ability.impl.fire;

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
 * Fire Passive — fire/lava immunity + flame particles.
 * Damage immunity handled by FirePassiveListener via EntityDamageEvent.
 * onTick extinguishes fire and spawns ambient particles.
 */
public class FirePassive implements PassiveAbility {

    @Override
    public Element getElement() {
        return Element.FIRE;
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
     * - Extinguishes fire if player is burning
     * - Spawns occasional flame particles
     */
    @Override
    public void onTick(AbilityContext context) {
        Player player = context.getPlayer();

        // Extinguish fire
        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }

        // Occasional flame particles (~30% chance per tick cycle)
        if (ThreadLocalRandom.current().nextFloat() < 0.3f) {
            player.getWorld().spawnParticle(
                    Particle.FLAME,
                    player.getLocation().add(0, 0.5, 0),
                    3, 0.2, 0.3, 0.2, 0.01);
        }
    }
}
