package com.cavetale.skills.skill;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.combat.CombatSkill;
import com.cavetale.skills.skill.mining.MiningSkill;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public final class Skills {
    private final SkillsPlugin plugin;
    public final MiningSkill mining;
    public final CombatSkill combat;

    public Skills(final SkillsPlugin plugin) {
        this.plugin = plugin;
        this.mining = new MiningSkill(plugin);
        this.combat = new CombatSkill(plugin);
    }

    public void enable() {
        for (SkillType skillType : SkillType.values()) {
            Skill skill = skillType.getSkill();
            if (skill == null) {
                plugin.getLogger().warning("Skill not implemented: " + skillType);
                continue;
            }
            skill.enable();
            if (skill instanceof Listener listener) {
                Bukkit.getPluginManager().registerEvents(listener, plugin);
            }
        }
        for (TalentType talentType : TalentType.values()) {
            Talent talent = talentType.getTalent();
            if (talent == null) {
                plugin.getLogger().warning("Talent not implemented: " + talentType);
                continue;
            }
            talent.enable();
            if (talent instanceof Listener listener) {
                Bukkit.getPluginManager().registerEvents(listener, plugin);
            }
        }
    }
}
