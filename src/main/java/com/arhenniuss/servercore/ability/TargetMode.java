package com.arhenniuss.servercore.ability;

/**
 * Defines how an ability selects its targets.
 * AbilityService uses this to determine targeting logic.
 */
public enum TargetMode {

    /** Single target via ray trace from player's eye location. */
    SINGLE,

    /** Area of effect — all living entities within configured radius. */
    AOE
}
