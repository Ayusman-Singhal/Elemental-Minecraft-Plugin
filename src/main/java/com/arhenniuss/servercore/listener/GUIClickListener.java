package com.arhenniuss.servercore.listener;

import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.gui.ElementSelectionGUI;
import com.arhenniuss.servercore.player.PlayerData;
import com.arhenniuss.servercore.player.PlayerDataManager;
import com.arhenniuss.servercore.util.ChatUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * Handles clicks in the Element Selection GUI.
 * Assigns the chosen element to the player's data.
 * No items are given — abilities are triggered via input only (Phase 11.5+).
 */
public class GUIClickListener implements Listener {

        private final PlayerDataManager playerDataManager;

        public GUIClickListener(PlayerDataManager playerDataManager) {
                this.playerDataManager = playerDataManager;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
                if (!(event.getView().getTopInventory().getHolder() instanceof ElementSelectionGUI)) {
                        return;
                }

                event.setCancelled(true);

                if (!(event.getWhoClicked() instanceof Player player)) {
                        return;
                }

                if (event.getClickedInventory() != event.getView().getTopInventory()) {
                        return;
                }

                Element element = ElementSelectionGUI.getElementFromSlot(event.getRawSlot());
                if (element == null) {
                        return;
                }

                PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
                if (data == null || data.hasElement()) {
                        player.closeInventory();
                        return;
                }

                data.setElement(element);
                playerDataManager.savePlayer(player.getUniqueId());
                player.closeInventory();

                // Feedback
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                player.sendMessage(ChatUtil.format("§aYou have chosen " + element.getColoredName() + "§a!"));
                player.sendMessage(ChatUtil.format(
                                "§7Use §eQ §7for basic, §eSneak+Q §7for secondary, §eF §7for special, §eDouble Jump §7for mobility."));
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event) {
                if (event.getView().getTopInventory().getHolder() instanceof ElementSelectionGUI) {
                        event.setCancelled(true);
                }
        }
}
