package com.cavetale.skills;

import lombok.NonNull;

public enum SkillType {
    MINING,
    FARMING;

    public final String key;
    public final String displayName;

    SkillType() {
        key = name().toLowerCase();
        displayName = name().substring(0, 1)
            + name().substring(1).toLowerCase();
    }

    static SkillType ofKey(@NonNull String key) {
        for (SkillType s : SkillType.values()) {
            if (key.equals(s.key)) return s;
        }
        return null;
    }
}
