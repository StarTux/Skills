package com.cavetale.skills;

import lombok.Getter;
import lombok.NonNull;

@Getter
public enum SkillType {
    MINING,
    FARMING,
    COMBAT;

    public final String key;
    public final String displayName;

    SkillType() {
        key = name().toLowerCase();
        displayName = Util.niceEnumName(this);
    }

    static SkillType ofKey(@NonNull String key) {
        for (SkillType s : SkillType.values()) {
            if (key.equals(s.key)) return s;
        }
        return null;
    }
}
