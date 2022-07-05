package com.cavetale.skills.skill.mining;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;

public final class DeepVisionTalent extends Talent {
    protected  final MiningSkill miningSkill;

    protected DeepVisionTalent(final SkillsPlugin plugin, final MiningSkill miningSkill) {
        super(plugin, TalentType.DEEP_VISION);
        this.miningSkill = miningSkill;
    }

    @Override
    protected void enable() { }
}
