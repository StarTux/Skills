package com.cavetale.skills.skill;

import com.cavetale.skills.SkillsPlugin;
import lombok.Getter;

public abstract class Talent {
    protected final SkillsPlugin plugin;
    @Getter protected final TalentType talentType;

    protected Talent(final SkillsPlugin plugin, final TalentType talentType) {
        this.plugin = plugin;
        this.talentType = talentType;
        talentType.register(this);
    }

    protected abstract void enable();
}
