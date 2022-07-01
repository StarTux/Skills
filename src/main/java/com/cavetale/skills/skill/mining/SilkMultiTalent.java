package com.cavetale.skills.skill.mining;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;

public final class SilkMultiTalent extends Talent {
    protected final MiningSkill miningSkill;

    protected SilkMultiTalent(final SkillsPlugin plugin, final MiningSkill miningSkill) {
        super(plugin, TalentType.SILK_MULTI);
        this.miningSkill = miningSkill;
    }

    @Override
    protected void enable() { }
}
