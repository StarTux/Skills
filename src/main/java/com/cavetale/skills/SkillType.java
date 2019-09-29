package com.cavetale.skills;

import lombok.NonNull;

public enum SkillType {
    MINING;

    public final String key;

    SkillType() {
        key = name().toLowerCase();
    }

    static SkillType ofKey(@NonNull String key) {
        for (SkillType s : SkillType.values()) {
            if (key.equals(s.key)) return s;
        }
        return null;
    }
}
