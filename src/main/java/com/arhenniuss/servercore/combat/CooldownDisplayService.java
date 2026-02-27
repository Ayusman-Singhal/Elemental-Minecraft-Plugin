package com.arhenniuss.servercore.combat;

import com.arhenniuss.servercore.ability.CooldownManager;
import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sends cooldown HUD via Action Bar every 10 ticks.
 * Shows Basic / Secondary / Special / Mobility cooldowns.
 * Plays a ready ping when a cooldown expires.
 */
public class CooldownDisplayService {

    private final CooldownManager cooldownManager;
    private final PlayerDataManager playerDataManager;
    private final Map<UUID, CooldownManager.CooldownSnapshot> lastSnapshots;
    private final StringBuilder sb;

    public CooldownDisplayService(CooldownManager cooldownManager,
            PlayerDataManager playerDataManager) {
        this.cooldownManager = cooldownManager;
        this.playerDataManager = playerDataManager;
        this.lastSnapshots = new HashMap<>();
        this.sb = new StringBuilder(120);
    }

    /**
     * Called every 10 ticks (0.5s) by the global task.
     * Sends cooldown HUD if changed. Plays ready ping on transition.
     */
    public void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Element element = playerDataManager.getElement(player.getUniqueId());
            if (element == null)
                continue;

            CooldownManager.CooldownSnapshot snapshot = cooldownManager.getSnapshot(player);
            CooldownManager.CooldownSnapshot last = lastSnapshots.get(player.getUniqueId());

            // Only send if changed (or first time)
            if (last != null && snapshot.equals(last)) {
                continue;
            }

            // Ready ping: if any slot transitions from on-cooldown → ready
            if (last != null) {
                if (last.basicRemaining() > 0 && snapshot.basicRemaining() == 0
                        || last.secondaryRemaining() > 0 && snapshot.secondaryRemaining() == 0
                        || last.specialRemaining() > 0 && snapshot.specialRemaining() == 0
                        || last.mobilityRemaining() > 0 && snapshot.mobilityRemaining() == 0) {
                    player.playSound(player.getLocation(),
                            Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 1.8f);
                }
            }

            lastSnapshots.put(player.getUniqueId(), snapshot);
            player.sendActionBar(buildActionBar(snapshot, element));
        }
    }

    private Component buildActionBar(CooldownManager.CooldownSnapshot snapshot, Element element) {
        sb.setLength(0);
        String color = element.getColorCode();

        sb.append(color).append("⚔ ");
        appendCooldown(sb, snapshot.basicRemaining());
        sb.append(" §8| ").append(color).append("⚡ ");
        appendCooldown(sb, snapshot.secondaryRemaining());
        sb.append(" §8| ").append(color).append("✦ ");
        appendCooldown(sb, snapshot.specialRemaining());
        sb.append(" §8| ").append(color).append("➤ ");
        appendCooldown(sb, snapshot.mobilityRemaining());

        return Component.text(sb.toString());
    }

    private void appendCooldown(StringBuilder sb, long remainingMs) {
        if (remainingMs <= 0) {
            sb.append("§a✓");
        } else {
            double seconds = remainingMs / 1000.0;
            sb.append("§e").append(String.format("%.1f", seconds));
        }
    }

    /**
     * Clears cached snapshot on player quit.
     */
    public void clearPlayer(UUID uuid) {
        lastSnapshots.remove(uuid);
    }
}
