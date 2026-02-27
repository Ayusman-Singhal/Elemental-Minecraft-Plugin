package com.arhenniuss.servercore.combat;

import com.arhenniuss.servercore.ability.Ability;
import com.arhenniuss.servercore.ability.AbilityContext;
import com.arhenniuss.servercore.combat.reaction.ReactionResult;
import com.arhenniuss.servercore.config.AbilityStats;
import com.arhenniuss.servercore.util.ChatUtil;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages debug mode toggles and structured debug output for ability execution.
 */
public class AbilityDebugManager {

    private final Set<UUID> debugEnabledPlayers;

    public AbilityDebugManager() {
        this.debugEnabledPlayers = new HashSet<>();
    }

    /**
     * Toggles debug mode for a player.
     *
     * @return true if debug is now enabled, false if disabled
     */
    public boolean toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (debugEnabledPlayers.contains(uuid)) {
            debugEnabledPlayers.remove(uuid);
            return false;
        } else {
            debugEnabledPlayers.add(uuid);
            return true;
        }
    }

    /**
     * Checks whether debug mode is enabled for a player.
     */
    public boolean isDebugEnabled(Player player) {
        return debugEnabledPlayers.contains(player.getUniqueId());
    }

    /**
     * Sends structured debug information after ability execution.
     */
    public void sendDebug(Player player, Ability ability, AbilityStats stats,
            double finalDamage, long remainingCooldown) {
        player.sendMessage(ChatUtil.format("§8[§bDEBUG§8] §e" + ability.getElement().name()
                + " → " + formatTypeName(ability.getType().name())));
        player.sendMessage(ChatUtil.format("§7  Category: §f" + ability.getCooldownCategory().name()));
        player.sendMessage(ChatUtil.format("§7  Base Damage: §f" + stats.damage()));
        player.sendMessage(ChatUtil.format("§7  Final Damage: §f" + String.format("%.1f", finalDamage)));
        player.sendMessage(ChatUtil.format("§7  Cooldown: §f" + remainingCooldown + "ms"));
        if (stats.knockback() > 0) {
            player.sendMessage(ChatUtil.format("§7  Knockback: §f" + stats.knockback()));
        }
        if (stats.radius() > 0) {
            player.sendMessage(ChatUtil.format("§7  Radius: §f" + stats.radius()));
        }
        if (stats.reach() > 0) {
            player.sendMessage(ChatUtil.format("§7  Reach: §f" + stats.reach()));
        }
    }

    /**
     * Sends debug information about triggered reactions.
     */
    public void sendReactionDebug(Player player, ReactionResult result) {
        player.sendMessage(ChatUtil.format("§8[§dREACTION§8] §6Triggered:"));
        for (String name : result.getTriggeredReactionNames()) {
            player.sendMessage(ChatUtil.format("§7  ⚡ §f" + name));
        }
        if (result.getTotalBonusDamage() > 0) {
            player.sendMessage(ChatUtil.format("§7  Bonus Damage: §c+"
                    + String.format("%.1f", result.getTotalBonusDamage())));
        }
    }

    /**
     * Sends debug information about mobility execution.
     */
    public void sendMobilityDebug(Player player, AbilityContext context) {
        double magnitude = context.getSelfVelocity() != null
                ? context.getSelfVelocity().length()
                : 0;
        player.sendMessage(ChatUtil.format("§8[§aMOBILITY§8] §6Self-Targeted"));
        player.sendMessage(ChatUtil.format("§7  Velocity: §f"
                + String.format("%.2f", magnitude)));
        if (context.getImmunityTicks() > 0) {
            player.sendMessage(ChatUtil.format("§7  Immunity: §f"
                    + context.getImmunityTicks() + " ticks"));
        }
    }

    /**
     * Formats ability type names for display.
     * e.g. SPECIAL_CHARGED → Special Charged
     */
    private String formatTypeName(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty())
                sb.append(" ");
            sb.append(Character.toUpperCase(part.charAt(0)));
            sb.append(part.substring(1));
        }
        return sb.toString();
    }

    /**
     * Removes a player from debug set on quit.
     */
    public void clearPlayer(UUID uuid) {
        debugEnabledPlayers.remove(uuid);
    }
}
