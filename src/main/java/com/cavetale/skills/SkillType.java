package com.cavetale.skills;

import com.cavetale.skills.util.Msg;
import lombok.NonNull;

public enum SkillType {
    MINING,
    COMBAT,
    FARMING;

    public final String key;
    public final String displayName;

    SkillType() {
        key = name().toLowerCase();
        displayName = Msg.enumToCamelCase(this);
    }

    public static SkillType ofKey(@NonNull String key) {
        for (SkillType s : SkillType.values()) {
            if (key.equals(s.key)) return s;
        }
        return null;
    }
}
