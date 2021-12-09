package com.cavetale.skills.skill;

import com.cavetale.skills.Util;
import lombok.Getter;
import lombok.NonNull;

@Getter
public enum SkillType {
    MINING,
    FARMING,
    COMBAT;

    public final String key;
    public final String displayName;
    private Skill skill;

    SkillType() {
        this.key = name().toLowerCase();
        this.displayName = Util.niceEnumName(this);
    }

    public static SkillType ofKey(@NonNull String key) {
        for (SkillType s : SkillType.values()) {
            if (key.equals(s.key)) return s;
        }
        return null;
    }

    protected void register(final Skill theSkill) {
        this.skill = theSkill;
    }
}
