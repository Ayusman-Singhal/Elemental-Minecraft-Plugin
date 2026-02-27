package com.arhenniuss.servercore.element;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

public enum Element {

    FIRE("Fire", "§c", NamedTextColor.RED, Material.BLAZE_POWDER, 10),
    WATER("Water", "§b", NamedTextColor.AQUA, Material.HEART_OF_THE_SEA, 12),
    EARTH("Earth", "§2", NamedTextColor.DARK_GREEN, Material.GRASS_BLOCK, 14),
    AIR("Air", "§f", NamedTextColor.WHITE, Material.FEATHER, 16);

    private final String displayName;
    private final String colorCode;
    private final TextColor textColor;
    private final Material material;
    private final int slot;

    Element(String displayName, String colorCode, TextColor textColor, Material material, int slot) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.textColor = textColor;
        this.material = material;
        this.slot = slot;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public TextColor getTextColor() {
        return textColor;
    }

    public Material getMaterial() {
        return material;
    }

    public int getSlot() {
        return slot;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    /**
     * Look up an Element by its name (case-insensitive).
     *
     * @param name the element name
     * @return the matching Element, or null if not found
     */
    public static Element fromName(String name) {
        if (name == null)
            return null;
        for (Element element : values()) {
            if (element.name().equalsIgnoreCase(name)) {
                return element;
            }
        }
        return null;
    }

    /**
     * Look up an Element by its GUI slot number.
     *
     * @param slot the inventory slot
     * @return the matching Element, or null if no element occupies that slot
     */
    public static Element fromSlot(int slot) {
        for (Element element : values()) {
            if (element.slot == slot) {
                return element;
            }
        }
        return null;
    }
}
