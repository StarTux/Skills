package com.cavetale.skills.skill;

import com.cavetale.skills.SkillsPlugin;
import lombok.Getter;

public abstract class Skill {
    protected final SkillsPlugin plugin;
    @Getter protected final SkillType skillType;

    protected Skill(final SkillsPlugin plugin, final SkillType skillType) {
        this.plugin = plugin;
        this.skillType = skillType;
        skillType.register(this);
    }

    protected void enable() { }
}
