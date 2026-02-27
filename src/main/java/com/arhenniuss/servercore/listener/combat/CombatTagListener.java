package com.arhenniuss.servercore.listener.combat;

import com.arhenniuss.servercore.combat.CombatTagManager;
import com.arhenniuss.servercore.combat.DamageAttributionManager;
import com.arhenniuss.servercore.config.AbilityBalanceConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Tags players in PvP combat and applies configurable logout penalty.
 */
public class CombatTagListener implements Listener {

    private final CombatTagManager combatTagManager;
    private final DamageAttributionManager damageManager;
    private final AbilityBalanceConfig config;

    public CombatTagListener(CombatTagManager combatTagManager,
            DamageAttributionManager damageManager,
            AbilityBalanceConfig config) {
        this.combatTagManager = combatTagManager;
        this.damageManager = damageManager;
        this.config = config;
    }

    @EventHandler
    public void onPvpDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker))
            return;
        if (!(event.getEntity() instanceof Player victim))
            return;
        if (attacker.equals(victim))
            return;

        // Tag both players
        combatTagManager.tag(attacker, victim.getUniqueId());
        combatTagManager.tag(victim, attacker.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (combatTagManager.shouldApplyLogoutPenalty(player.getUniqueId())) {
            String penalty = config.getLogoutPenalty();

            switch (penalty) {
                case "KILL" -> player.setHealth(0);
                case "STRIP_HEALTH" -> player.setHealth(1);
                // NONE → do nothing
            }
        }

        // Cleanup
        combatTagManager.clearTag(player.getUniqueId());
        damageManager.clearAll(player.getUniqueId());
    }
}
