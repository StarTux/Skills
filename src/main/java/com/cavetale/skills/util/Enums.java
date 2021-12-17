package com.cavetale.skills.util;

import lombok.NonNull;

public final class Enums {
    private Enums() { }

    private static String camelCase(@NonNull String name, @NonNull final String glue) {
        String[] toks = name.split("_");
        for (int i = 0; i < toks.length; i += 1) {
            toks[i] = toks[i].substring(0, 1) + toks[i].substring(1).toLowerCase();
        }
        return String.join(glue, toks);
    }

    public static String human(@NonNull Enum en) {
        return camelCase(en.name(), " ");
    }
}
