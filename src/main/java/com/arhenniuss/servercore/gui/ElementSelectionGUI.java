package com.arhenniuss.servercore.gui;

import com.arhenniuss.servercore.element.Element;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ElementSelectionGUI implements InventoryHolder {

    private static final Component TITLE = Component.text("Choose Your Element", NamedTextColor.DARK_GRAY);
    private static final int SIZE = 27;

    /**
     * Not used — we create a fresh inventory per open() call.
     * Required by InventoryHolder contract.
     */
    @Override
    public Inventory getInventory() {
        return Bukkit.createInventory(this, SIZE, TITLE);
    }

    /**
     * Creates a brand-new inventory for the given player, populates
     * element items, and opens it. Each call produces a unique Inventory
     * instance (no shared state between players).
     *
     * @param player the player to open the GUI for
     */
    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(this, SIZE, TITLE);

        for (Element element : Element.values()) {
            inventory.setItem(element.getSlot(), createElementItem(element));
        }

        player.openInventory(inventory);
    }

    /**
     * Returns the Element corresponding to the given slot, or null
     * if no element occupies that slot.
     *
     * @param slot the inventory slot number
     * @return the matching Element, or null
     */
    public static Element getElementFromSlot(int slot) {
        return Element.fromSlot(slot);
    }

    /**
     * Builds a display ItemStack for the given element with enchant glow
     * and a descriptive lore line. Uses Adventure API (non-deprecated).
     */
    private ItemStack createElementItem(Element element) {
        ItemStack item = new ItemStack(element.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(element.getDisplayName(), element.getTextColor())
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Click to choose ", NamedTextColor.GRAY)
                            .append(Component.text(element.getDisplayName(), element.getTextColor()))
                            .decoration(TextDecoration.ITALIC, false)));

            // Enchant glow without visible enchantment text
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            item.setItemMeta(meta);
        }

        return item;
    }
}
