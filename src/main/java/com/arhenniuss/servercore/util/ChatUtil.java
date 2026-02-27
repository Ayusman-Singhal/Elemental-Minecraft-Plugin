package com.arhenniuss.servercore.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class ChatUtil {

    private static final String PREFIX = "§8[§6ServerCore§8] §7";

    private ChatUtil() {
        // Utility class
    }

    /**
     * Formats a message with the plugin prefix.
     *
     * @param message the raw message
     * @return the prefixed message
     */
    public static String format(String message) {
        return PREFIX + message;
    }

    /**
     * Formats a message and returns it as an Adventure Component.
     *
     * @param message the raw message (may contain § color codes)
     * @return the formatted Component
     */
    public static Component formatComponent(String message) {
        return LegacyComponentSerializer.legacySection().deserialize(PREFIX + message);
    }
}
