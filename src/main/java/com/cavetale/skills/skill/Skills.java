package com.cavetale.skills.skill;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.combat.CombatSkill;
import com.cavetale.skills.skill.farming.FarmingSkill;
import com.cavetale.skills.skill.mining.MiningSkill;

public final class Skills {
    private final SkillsPlugin plugin;
    public final MiningSkill mining;
    public final FarmingSkill farming;
    public final CombatSkill combat;

    public Skills(final SkillsPlugin plugin) {
        this.plugin = plugin;
        this.mining = new MiningSkill(plugin);
        this.farming = new FarmingSkill(plugin);
        this.combat = new CombatSkill(plugin);
    }

    public void enable() {
        for (SkillType skillType : SkillType.values()) {
            skillType.getSkill().enable();
        }
    }
}
