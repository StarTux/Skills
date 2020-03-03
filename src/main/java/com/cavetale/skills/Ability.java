package com.cavetale.skills;

import java.util.HashMap;
import lombok.NonNull;

public enum Ability {
    STRIP_MINING;

    public final String key;
    public final String displayName;
    private static final HashMap<String, Ability> KEY_MAP = new HashMap<>();

    static {
        for (Ability ability : values()) {
            KEY_MAP.put(ability.key, ability);
        }
    }

    Ability() {
        key = name().toLowerCase();
        this.displayName = Msg.enumToCamelCase(this);
    }

    public static Ability of(@NonNull String key) {
        return KEY_MAP.get(key);
    }
}
