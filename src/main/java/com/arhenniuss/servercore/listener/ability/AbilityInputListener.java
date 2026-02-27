package com.arhenniuss.servercore.listener.ability;

import com.arhenniuss.servercore.ability.AbilityService;
import com.arhenniuss.servercore.ability.AbilityType;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

/**
 * Single authoritative input gateway for all ability execution.
 *
 * Input mapping (Phase 11.5):
 * Q (Drop) + empty hand → BASIC
 * Sneak + Q + empty hand → SECONDARY
 * F (Swap Hand) → SPECIAL_SIMPLE
 * Sneak + F → SPECIAL_CHARGED
 * Double Jump (Toggle Flight) → MOBILITY
 *
 * This listener is INPUT ONLY — zero combat, velocity, or status logic.
 * All execution is handled by AbilityService.
 */
public class AbilityInputListener implements Listener {

    private final AbilityService abilityService;

    public AbilityInputListener(AbilityService abilityService) {
        this.abilityService = abilityService;
    }

    /**
     * Q key (Drop) — Basic / Secondary abilities.
     * Only triggers when main hand is empty (AIR).
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Resolve type based on sneak
        AbilityType type = player.isSneaking()
                ? AbilityType.SECONDARY
                : AbilityType.BASIC;

        boolean executed = abilityService.tryExecute(player, type);
        if (executed) {
            // Cancel the drop — ability consumed the input
            event.setCancelled(true);
        }
        // If not executed, item drops normally (vanilla behavior preserved)
    }

    /**
     * F key (Swap Hand Items) — Special abilities.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        // Resolve type based on sneak
        AbilityType type = player.isSneaking()
                ? AbilityType.SPECIAL_CHARGED
                : AbilityType.SPECIAL_SIMPLE;

        boolean executed = abilityService.tryExecute(player, type);
        if (executed) {
            event.setCancelled(true);
        }
    }

    /**
     * Double Jump (Toggle Flight) — Mobility.
     * Requires player to be on ground (allowFlight set by ground tick).
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        // Ignore creative/spectator — they have real flight
        if (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Cancel the flight toggle (we use it as double-jump input)
        event.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);

        abilityService.tryExecute(player, AbilityType.MOBILITY);
    }
}
