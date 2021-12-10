package com.cavetale.skills.skill.mining;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;

public final class SilkFortuneTalent extends Talent {
    protected final MiningSkill miningSkill;

    protected SilkFortuneTalent(final SkillsPlugin plugin, final MiningSkill miningSkill) {
        super(plugin, TalentType.MINE_SILK_MULTI);
        this.miningSkill = miningSkill;
    }

    @Override
    protected void enable() { }
}
