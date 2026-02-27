package com.arhenniuss.servercore.ability;

import com.arhenniuss.servercore.element.Element;

import java.util.EnumMap;
import java.util.Map;

public class AbilityRegistry {

    private final Map<Element, Map<AbilityType, Ability>> registry;

    public AbilityRegistry() {
        this.registry = new EnumMap<>(Element.class);
    }

    /**
     * Registers an ability. Overwrites any existing ability for the same
     * element + type combination.
     */
    public void register(Ability ability) {
        registry.computeIfAbsent(ability.getElement(), k -> new EnumMap<>(AbilityType.class))
                .put(ability.getType(), ability);
    }

    /**
     * Retrieves an ability for the given element and type.
     *
     * @return the Ability, or null if not registered
     */
    public Ability getAbility(Element element, AbilityType type) {
        Map<AbilityType, Ability> elementAbilities = registry.get(element);
        if (elementAbilities == null)
            return null;
        return elementAbilities.get(type);
    }

    /**
     * Retrieves the passive ability for the given element.
     *
     * @return the PassiveAbility, or null if not registered
     */
    public PassiveAbility getPassive(Element element) {
        Ability ability = getAbility(element, AbilityType.PASSIVE);
        if (ability instanceof PassiveAbility passive) {
            return passive;
        }
        return null;
    }
}
