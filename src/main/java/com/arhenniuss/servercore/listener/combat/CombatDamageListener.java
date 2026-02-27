package com.arhenniuss.servercore.listener.combat;

import com.arhenniuss.servercore.combat.DamageAttributionManager;
import com.arhenniuss.servercore.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Handles kill attribution from ability damage and custom death messages.
 */
public class CombatDamageListener implements Listener {

    private final DamageAttributionManager damageManager;

    public CombatDamageListener(DamageAttributionManager damageManager) {
        this.damageManager = damageManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        DamageAttributionManager.DamageRecord record = damageManager.getAttribution(victim.getUniqueId());

        if (record != null) {
            Player attacker = Bukkit.getPlayer(record.attacker());
            if (attacker != null) {
                String abilityName = record.abilityType().name().replace("_", " ");
                event.deathMessage(null); // suppress default
                Bukkit.broadcast(ChatUtil.formatComponent(
                        "§c" + victim.getName() + " §7was slain by §c" + attacker.getName()
                                + " §7using §e" + abilityName));
            }
        }

        // Always clear on death
        damageManager.clearAttribution(victim.getUniqueId());
    }
}
