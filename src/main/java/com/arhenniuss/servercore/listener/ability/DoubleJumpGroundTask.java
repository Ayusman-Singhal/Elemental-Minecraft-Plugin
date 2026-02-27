package com.arhenniuss.servercore.listener.ability;

import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Ground tick task — enables allowFlight when survival/adventure players
 * are on the ground, allowing double-jump input via PlayerToggleFlightEvent.
 *
 * Runs every 4 ticks (200ms). Only affects players with an assigned element.
 * Resets allowFlight when player is airborne to prevent actual flight.
 */
public class DoubleJumpGroundTask implements Runnable {

    private final PlayerDataManager playerDataManager;

    public DoubleJumpGroundTask(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void register(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 4L);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Skip creative/spectator — they have real flight
            if (player.getGameMode() == GameMode.CREATIVE
                    || player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            // Only affect players with an element
            Element element = playerDataManager.getElement(player.getUniqueId());
            if (element == null)
                continue;

            // On ground → enable allowFlight (allows double-jump input)
            if (player.isOnGround()) {
                if (!player.getAllowFlight()) {
                    player.setAllowFlight(true);
                }
            }
            // Don't disable in air — the toggle event handler does that
        }
    }
}
