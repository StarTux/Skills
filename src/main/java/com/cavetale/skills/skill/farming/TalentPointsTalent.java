package com.cavetale.skills.skill.farming;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;

public final class TalentPointsTalent extends Talent {
    protected final FarmingSkill farmingSkill;

    protected TalentPointsTalent(final SkillsPlugin plugin, final FarmingSkill farmingSkill) {
        super(plugin, TalentType.FARM_TALENT_POINTS);
        this.farmingSkill = farmingSkill;
    }

    @Override
    protected void enable() { }
}
