package com.cavetale.skills.skill;

import com.cavetale.skills.skill.archery.ArcherySkill;
import com.cavetale.skills.skill.combat.CombatSkill;
import com.cavetale.skills.skill.mining.MiningSkill;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class Skills {
    public final MiningSkill mining = new MiningSkill();
    public final CombatSkill combat = new CombatSkill();
    public final ArcherySkill archery = new ArcherySkill();

    public void enable() {
        for (SkillType skillType : SkillType.values()) {
            Skill skill = skillType.getSkill();
            if (skill == null) {
                skillsPlugin().getLogger().warning("Skill not implemented: " + skillType);
                continue;
            }
            skill.enable();
            if (skill instanceof Listener listener) {
                Bukkit.getPluginManager().registerEvents(listener, skillsPlugin());
            }
        }
        for (TalentType talentType : TalentType.values()) {
            if (!talentType.isEnabled()) {
                skillsPlugin().getLogger().warning("Talent not implemented: " + talentType);
                continue;
            }
            if (talentType.getTalent() instanceof Listener listener) {
                Bukkit.getPluginManager().registerEvents(listener, skillsPlugin());
            }
        }
    }
}
