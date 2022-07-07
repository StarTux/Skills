package com.cavetale.skills.skill;

import com.cavetale.skills.skill.combat.CombatSkill;
import com.cavetale.skills.skill.mining.MiningSkill;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class Skills {
    public final MiningSkill mining = new MiningSkill();
    public final CombatSkill combat = new CombatSkill();

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
            Talent talent = talentType.getTalent();
            if (talent == null) {
                skillsPlugin().getLogger().warning("Talent not implemented: " + talentType);
                continue;
            }
            if (talent instanceof Listener listener) {
                Bukkit.getPluginManager().registerEvents(listener, skillsPlugin());
            }
        }
    }
}
