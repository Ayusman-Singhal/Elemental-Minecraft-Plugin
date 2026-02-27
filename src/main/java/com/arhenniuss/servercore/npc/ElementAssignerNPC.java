package com.arhenniuss.servercore.npc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ElementAssignerNPC {

    private final NamespacedKey npcKey;

    public ElementAssignerNPC(NamespacedKey npcKey) {
        this.npcKey = npcKey;
    }

    /**
     * Spawns an Element Assigner NPC at the given location.
     * The villager is fully locked down: no AI, invulnerable, silent,
     * non-collidable, profession NONE, level 1, no trades, persistent.
     *
     * @param location the spawn location
     * @return the spawned Villager entity
     */
    public Villager spawn(Location location) {
        Villager villager = location.getWorld().spawn(location, Villager.class, v -> {
            // Identity — uses Adventure API (non-deprecated)
            v.customName(Component.text("Element Assigner", NamedTextColor.GOLD));
            v.setCustomNameVisible(true);

            // Lock down
            v.setAI(false);
            v.setInvulnerable(true);
            v.setSilent(true);
            v.setCollidable(false);

            // Villager specifics
            v.setProfession(Villager.Profession.NONE);
            v.setVillagerLevel(1);
            v.setRecipes(List.of());

            // Persistence
            v.setPersistent(true);
            v.setRemoveWhenFarAway(false);

            // PDC tag for identification
            PersistentDataContainer pdc = v.getPersistentDataContainer();
            pdc.set(npcKey, PersistentDataType.BOOLEAN, true);
        });

        return villager;
    }

    /**
     * Checks whether the given entity is an Element Assigner NPC
     * by inspecting its PersistentDataContainer. Never compares name strings.
     *
     * @param entity the entity to check
     * @return true if the entity is an Element Assigner
     */
    public boolean isElementAssigner(Entity entity) {
        if (entity == null)
            return false;
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        return pdc.has(npcKey, PersistentDataType.BOOLEAN);
    }
}
