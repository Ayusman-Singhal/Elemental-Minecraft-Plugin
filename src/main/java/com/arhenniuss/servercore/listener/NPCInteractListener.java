package com.arhenniuss.servercore.listener;

import com.arhenniuss.servercore.gui.ElementSelectionGUI;
import com.arhenniuss.servercore.npc.ElementAssignerNPC;
import com.arhenniuss.servercore.player.PlayerData;
import com.arhenniuss.servercore.player.PlayerDataManager;
import com.arhenniuss.servercore.util.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class NPCInteractListener implements Listener {

    private final ElementAssignerNPC assignerNPC;
    private final PlayerDataManager playerDataManager;
    private final ElementSelectionGUI gui;

    public NPCInteractListener(ElementAssignerNPC assignerNPC,
            PlayerDataManager playerDataManager,
            ElementSelectionGUI gui) {
        this.assignerNPC = assignerNPC;
        this.playerDataManager = playerDataManager;
        this.gui = gui;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!assignerNPC.isElementAssigner(event.getRightClicked())) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());

        if (data == null) {
            player.sendMessage(ChatUtil.format("§cYour data is not loaded. Please relog."));
            return;
        }

        if (data.hasElement()) {
            player.sendMessage(
                    ChatUtil.format("§cYou have already chosen §e" + data.getElement().getColoredName() + "§c."));
            return;
        }

        gui.open(player);
    }
}
