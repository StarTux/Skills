package com.cavetale.skills.skill.farming;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;

public final class CropDropsTalent extends Talent {
    protected final FarmingSkill farmingSkill;

    protected CropDropsTalent(final SkillsPlugin plugin, final FarmingSkill farmingSkill) {
        super(plugin, TalentType.FARM_CROP_DROPS);
        this.farmingSkill = farmingSkill;
    }

    @Override
    protected void enable() { }
}
