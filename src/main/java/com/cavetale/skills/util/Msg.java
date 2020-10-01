package com.cavetale.skills.util;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;

/**
 * Utiltiy for messaging and string manipulation.
 */
public final class Msg {
    private Msg() { }

    /**
     * Turn a String in Java Enum format into camel case, without
     * spaces.
     * E.g. GRASS_BLOCK => GrassBlock.
     */
    public static String enumToCamelCase(@NonNull String name) {
        return Stream.of(name.split("_"))
            .map(s -> s.substring(0, 1) + s.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    /**
     * Convenience override.
     */
    public static String enumToCamelCase(@NonNull Enum enom) {
        return enumToCamelCase(enom.name());
    }
}
