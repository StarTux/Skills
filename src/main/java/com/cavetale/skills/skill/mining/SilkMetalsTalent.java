package com.cavetale.skills.skill.mining;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;

public final class SilkMetalsTalent extends Talent {
    protected final MiningSkill miningSkill;

    protected SilkMetalsTalent(final SkillsPlugin plugin, final MiningSkill miningSkill) {
        super(plugin, TalentType.SILK_METALS);
        this.miningSkill = miningSkill;
    }

    @Override
    protected void enable() { }
}
