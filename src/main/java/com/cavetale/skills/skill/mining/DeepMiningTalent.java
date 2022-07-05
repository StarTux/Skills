package com.cavetale.skills.skill.mining;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;

public final class DeepMiningTalent extends Talent {
    protected  final MiningSkill miningSkill;

    protected DeepMiningTalent(final SkillsPlugin plugin, final MiningSkill miningSkill) {
        super(plugin, TalentType.DEEP_MINING);
        this.miningSkill = miningSkill;
    }

    @Override
    protected void enable() { }
}
