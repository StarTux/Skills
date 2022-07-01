package com.cavetale.skills.skill.mining;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;

public final class VeinGemsTalent extends Talent {
    protected final MiningSkill miningSkill;

    protected VeinGemsTalent(final SkillsPlugin plugin, final MiningSkill miningSkill) {
        super(plugin, TalentType.VEIN_GEMS);
        this.miningSkill = miningSkill;
    }

    @Override
    protected void enable() { }
}
