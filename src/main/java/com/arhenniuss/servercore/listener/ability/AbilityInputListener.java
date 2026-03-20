package com.arhenniuss.servercore.listener.ability;

import com.arhenniuss.servercore.ability.AbilityService;
import com.arhenniuss.servercore.ability.AbilityType;
import com.arhenniuss.servercore.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    private static final long SWAP_INPUT_COOLDOWN_MS = 150L;

    private final AbilityService abilityService;
    private final PlayerDataManager playerDataManager;
    private final Map<UUID, Long> lastDropInput;
    private final Map<UUID, Long> lastSwapInput;
    private final Map<UUID, Integer> inventoryDropTicks;

    public AbilityInputListener(AbilityService abilityService, PlayerDataManager playerDataManager) {
        this.abilityService = abilityService;
        this.playerDataManager = playerDataManager;
        this.lastDropInput = new HashMap<>();
        this.lastSwapInput = new HashMap<>();
        this.inventoryDropTicks = new HashMap<>();
    }

    /**
     * Intercepts drops originating from inside the inventory GUI to bypass ability triggers.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // If they click outside the window to drop the cursor item, or press Q while hovering
        if (event.getSlotType() == InventoryType.SlotType.OUTSIDE ||
            event.getClick() == ClickType.DROP ||
            event.getClick() == ClickType.CONTROL_DROP) {
            
            // Mark this player as currently doing an inventory drop in this tick
            inventoryDropTicks.put(player.getUniqueId(), Bukkit.getCurrentTick());
        }
    }

    /**
     * Q key (Drop) — Basic / Secondary abilities.
     * Only triggers when main hand is empty (AIR).
     * Cancels the event if player has an element (prevents item drop).
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Check if player has an element
        if (playerDataManager.getElement(player.getUniqueId()) == null) {
            return; // No element, allow vanilla drop
        }

        // Bypass: if the drop resulted from an inventory interaction in the same tick
        if (inventoryDropTicks.getOrDefault(player.getUniqueId(), -1) == Bukkit.getCurrentTick()) {
            return; // Allow the normal drop without triggering abilities
        }
        
        // Bypass: if looking straight down (pitch > 80 degrees), allow normal drop
        if (player.getLocation().getPitch() > 80f) {
            return;
        }

        long now = System.currentTimeMillis();
        Long lastDrop = lastDropInput.get(player.getUniqueId());

        // Bypass: Double-pressing Q (within 300ms) allows the item to drop natively
        if (lastDrop != null && now - lastDrop < 300) {
            lastDropInput.remove(player.getUniqueId()); // Reset to prevent chaining into another drop
            return; // Allow the item to actually drop
        }

        // Resolve type based on sneak
        AbilityType type = player.isSneaking()
                ? AbilityType.SECONDARY
                : AbilityType.BASIC;

        // At this point, it's a standard ability cast (first Q press). 
        // We cancel the drop and execute the ability instead.
        event.setCancelled(true);
        lastDropInput.put(player.getUniqueId(), now);

        // Try to execute the ability
        abilityService.tryExecute(player, type);
    }

    /**
     * F key (Swap Hand Items) — Special abilities.
     * Cancels the event if player has an element (prevents hand swap).
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        // Check if player has an element
        if (playerDataManager.getElement(player.getUniqueId()) == null) {
            return; // No element, allow vanilla hand swap
        }

        // Resolve type based on sneak
        AbilityType type = player.isSneaking()
                ? AbilityType.SPECIAL_CHARGED
                : AbilityType.SPECIAL_SIMPLE;

        // Always cancel the swap when player has an element
        // This prevents spamming from switching hands even if on cooldown
        event.setCancelled(true);

        // Throttle repeated F presses so cooldown-failed attempts cannot be spammed.
        long now = System.currentTimeMillis();
        if (isThrottled(lastSwapInput, player.getUniqueId(), now, SWAP_INPUT_COOLDOWN_MS)) {
            return;
        }

        // Try to execute the ability
        abilityService.tryExecute(player, type);
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

    private boolean isThrottled(Map<UUID, Long> tracker, UUID uuid, long now, long thresholdMs) {
        Long last = tracker.get(uuid);
        if (last != null && now - last < thresholdMs) {
            return true;
        }
        tracker.put(uuid, now);
        return false;
    }
}
