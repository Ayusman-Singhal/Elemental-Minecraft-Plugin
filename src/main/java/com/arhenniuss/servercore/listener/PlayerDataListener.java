package com.arhenniuss.servercore.listener;

import com.arhenniuss.servercore.ability.CooldownManager;
import com.arhenniuss.servercore.combat.AbilityDebugManager;
import com.arhenniuss.servercore.combat.CombatTagManager;
import com.arhenniuss.servercore.combat.CooldownDisplayService;
import com.arhenniuss.servercore.combat.DamageAttributionManager;
import com.arhenniuss.servercore.player.PlayerDataManager;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

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

        // Give element guide book on join
        ItemStack guideBook = createElementGuideBook();
        event.getPlayer().getInventory().addItem(guideBook);
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

    /**
     * Creates a written book containing element ability guide.
     */
    private ItemStack createElementGuideBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta == null) {
            return book;
        }

        meta.setAuthor("Element Masters");
    meta.setTitle("Element Abilities Guide");

    meta.addPages(
        Component.text(
            "Element Abilities Guide\n\n" +
            "Welcome, Element Master!\n\n" +
            "This guide will teach you how to harness the power of the elements.\n\n" +
            "Use Q for basic attacks, Shift+Q for secondary, F for special, and Shift+F for charged special."
        ),
        Component.text(
            "Basic Controls\n\n" +
            "Q - Basic Attack\n" +
            "Shift + Q - Secondary Attack\n\n" +
            "F - Special Ability\n" +
            "Shift + F - Charged Special\n\n" +
            "Space (in air) - Double Jump"
        ),
        Component.text(
            "The Four Elements\n\n" +
            "Fire - Offensive, burns enemies\n" +
            "Water - Defensive, healing\n" +
            "Earth - Tanky, crowd control\n" +
            "Air - Fast, ranged"
        ),
        Component.text(
            "Pro Tips\n\n" +
            "• Watch your cooldowns on the action bar\n" +
            "• Use double jump for mobility\n" +
            "• Practice combos with different elements\n" +
            "• Team up for ultimate attacks!"
        )
    );

        book.setItemMeta(meta);
        return book;
    }
}
