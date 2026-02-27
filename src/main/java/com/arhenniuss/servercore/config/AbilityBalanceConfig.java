package com.arhenniuss.servercore.config;

import com.arhenniuss.servercore.ability.AbilityType;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;

/**
 * Loads and provides all ability balancing values from config.yml.
 * Supports reload without recreating managers.
 */
public class AbilityBalanceConfig {

    private final JavaPlugin plugin;
    private final Map<Element, Map<AbilityType, AbilityStats>> statsMap;
    private String logoutPenalty;

    public AbilityBalanceConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.statsMap = new EnumMap<>(Element.class);
        load();
    }

    /**
     * Reloads all balancing values from config.yml.
     * Does not affect cooldowns, combat tags, or any runtime state.
     */
    public void reload() {
        plugin.reloadConfig();
        statsMap.clear();
        load();
    }

    private void load() {
        plugin.saveDefaultConfig();

        // Load combat settings
        logoutPenalty = plugin.getConfig().getString("combat.logout_penalty", "KILL").toUpperCase();

        ConfigurationSection elements = plugin.getConfig().getConfigurationSection("elements");
        if (elements == null)
            return;

        for (String elementName : elements.getKeys(false)) {
            Element element;
            try {
                element = Element.valueOf(elementName.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown element in config: " + elementName);
                continue;
            }

            ConfigurationSection elementSection = elements.getConfigurationSection(elementName);
            if (elementSection == null)
                continue;

            Map<AbilityType, AbilityStats> abilityMap = new EnumMap<>(AbilityType.class);

            for (String typeName : elementSection.getKeys(false)) {
                AbilityType type = resolveAbilityType(typeName);
                if (type == null) {
                    plugin.getLogger().warning("Unknown ability type in config: " + typeName);
                    continue;
                }

                ConfigurationSection abilitySection = elementSection.getConfigurationSection(typeName);
                if (abilitySection == null)
                    continue;

                AbilityStats stats = new AbilityStats(
                        abilitySection.getLong("cooldown_ms", 1000),
                        abilitySection.getDouble("damage", 4.0),
                        abilitySection.getDouble("radius", 0),
                        abilitySection.getDouble("knockback", 0),
                        abilitySection.getDouble("reach", 3.0),
                        abilitySection.getDouble("velocity_magnitude", 0),
                        abilitySection.getDouble("velocity_y", 0),
                        abilitySection.getInt("immunity_ticks", 0));

                abilityMap.put(type, stats);
            }

            statsMap.put(element, abilityMap);
        }
    }

    /**
     * Gets the AbilityStats for a given element and ability type.
     * Returns sensible defaults if not configured.
     */
    public AbilityStats getStats(Element element, AbilityType type) {
        Map<AbilityType, AbilityStats> elementMap = statsMap.get(element);
        if (elementMap == null)
            return AbilityStats.of(1000, 4.0);
        AbilityStats stats = elementMap.get(type);
        return stats != null ? stats : AbilityStats.of(1000, 4.0);
    }

    /**
     * Returns the configured logout penalty mode: KILL, NONE, or STRIP_HEALTH.
     */
    public String getLogoutPenalty() {
        return logoutPenalty;
    }

    private AbilityType resolveAbilityType(String name) {
        return switch (name.toUpperCase()) {
            case "BASIC" -> AbilityType.BASIC;
            case "SECONDARY" -> AbilityType.SECONDARY;
            case "SPECIAL_SIMPLE" -> AbilityType.SPECIAL_SIMPLE;
            case "SPECIAL_CHARGED" -> AbilityType.SPECIAL_CHARGED;
            case "PASSIVE" -> AbilityType.PASSIVE;
            case "MOBILITY" -> AbilityType.MOBILITY;
            default -> null;
        };
    }
}
