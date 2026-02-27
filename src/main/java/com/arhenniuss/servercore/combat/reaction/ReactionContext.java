package com.arhenniuss.servercore.combat.reaction;

import com.arhenniuss.servercore.ability.AbilityType;
import com.arhenniuss.servercore.combat.status.StatusType;
import com.arhenniuss.servercore.element.Element;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;

/**
 * Immutable snapshot of ability resolution state, used for reaction evaluation.
 *
 * Built by AbilityService AFTER base damage + knockback + status application.
 * statusesBefore is captured BEFORE ability statuses are applied — this is
 * critical for detecting reactions like Fire on a WET target.
 */
public record ReactionContext(
        Player caster,
        LivingEntity target,
        Element elementUsed,
        AbilityType abilityType,
        double baseDamage,
        Set<StatusType> statusesBefore,
        Set<StatusType> statusesAppliedByAbility) {

    public ReactionContext {
        statusesBefore = Collections.unmodifiableSet(statusesBefore);
        statusesAppliedByAbility = Collections.unmodifiableSet(statusesAppliedByAbility);
    }
}
