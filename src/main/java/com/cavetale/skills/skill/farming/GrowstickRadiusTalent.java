package com.cavetale.skills.skill.farming;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;

public final class GrowstickRadiusTalent extends Talent {
    protected final FarmingSkill farmingSkill;

    protected GrowstickRadiusTalent(final SkillsPlugin plugin, final FarmingSkill farmingSkill) {
        super(plugin, TalentType.FARM_GROWSTICK_RADIUS);
        this.farmingSkill = farmingSkill;
    }

    @Override
    protected void enable() { }
}
