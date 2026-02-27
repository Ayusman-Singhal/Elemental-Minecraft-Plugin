package com.arhenniuss.servercore.listener;

import com.arhenniuss.servercore.ability.CooldownManager;
import com.arhenniuss.servercore.combat.AbilityDebugManager;
import com.arhenniuss.servercore.combat.CombatTagManager;
import com.arhenniuss.servercore.combat.CooldownDisplayService;
import com.arhenniuss.servercore.combat.DamageAttributionManager;
import com.arhenniuss.servercore.player.PlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerDataListener implements Listener {

    private final PlayerDataManager playerDataManager;
    private final CooldownManager cooldownManager;
    private final DamageAttributionManager damageManager;
    private final CombatTagManager combatTagManager;
    private final AbilityDebugManager debugManager;
    private final CooldownDisplayService cooldownDisplay;

    public PlayerDataListener(PlayerDataManager playerDataManager,
            CooldownManager cooldownManager,
            DamageAttributionManager damageManager,
            CombatTagManager combatTagManager,
            AbilityDebugManager debugManager,
            CooldownDisplayService cooldownDisplay) {
        this.playerDataManager = playerDataManager;
        this.cooldownManager = cooldownManager;
        this.damageManager = damageManager;
        this.combatTagManager = combatTagManager;
        this.debugManager = debugManager;
        this.cooldownDisplay = cooldownDisplay;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerDataManager.loadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        // Save and remove player data
        playerDataManager.saveAndRemove(uuid);

        // Clean up all state
        cooldownManager.clearAll(uuid);
        damageManager.clearAll(uuid);
        combatTagManager.clearTag(uuid);
        debugManager.clearPlayer(uuid);
        cooldownDisplay.clearPlayer(uuid);
    }
}
