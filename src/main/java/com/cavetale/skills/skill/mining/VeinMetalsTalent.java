package com.cavetale.skills.skill.mining;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;

public final class VeinMetalsTalent extends Talent {
    protected final MiningSkill miningSkill;

    protected VeinMetalsTalent(final SkillsPlugin plugin, final MiningSkill miningSkill) {
        super(plugin, TalentType.VEIN_METALS);
        this.miningSkill = miningSkill;
    }

    @Override
    protected void enable() { }
}
