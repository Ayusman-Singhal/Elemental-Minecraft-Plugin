package com.arhenniuss.servercore.combat.reaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Structured output from reaction processing.
 * Accumulates results from all triggered rules.
 */
public class ReactionResult {

    private final List<String> triggeredReactionNames = new ArrayList<>();
    private double totalBonusDamage = 0;

    public void addReaction(String name, double bonusDamage) {
        triggeredReactionNames.add(name);
        totalBonusDamage += bonusDamage;
    }

    public List<String> getTriggeredReactionNames() {
        return Collections.unmodifiableList(triggeredReactionNames);
    }

    public double getTotalBonusDamage() {
        return totalBonusDamage;
    }

    public boolean hasReactions() {
        return !triggeredReactionNames.isEmpty();
    }
}
