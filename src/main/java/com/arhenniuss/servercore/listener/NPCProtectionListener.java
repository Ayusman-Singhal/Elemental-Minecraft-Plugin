package com.arhenniuss.servercore.listener;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;
import org.bukkit.persistence.PersistentDataType;

public class NPCProtectionListener implements Listener {

    private final NamespacedKey npcKey;

    public NPCProtectionListener(NamespacedKey npcKey) {
        this.npcKey = npcKey;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (isAssigner(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if (isAssigner(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVillagerCareerChange(VillagerCareerChangeEvent event) {
        if (isAssigner(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVillagerReplenishTrade(VillagerReplenishTradeEvent event) {
        if (isAssigner(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityBreed(EntityBreedEvent event) {
        if (isAssigner(event.getMother()) || isAssigner(event.getFather())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (isAssigner(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    private boolean isAssigner(Entity entity) {
        if (entity == null)
            return false;
        return entity.getPersistentDataContainer().has(npcKey, PersistentDataType.BOOLEAN);
    }
}
