package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class SearingTalent extends Talent {
    protected final CombatSkill combatSkill;

    protected SearingTalent(final SkillsPlugin plugin, final CombatSkill combatSkill) {
        super(plugin, TalentType.SEARING);
        this.combatSkill = combatSkill;
        this.description = "Monsters set on fire deal -50% melee damage";
        this.infoPages = List.of();
    }

    @Override protected void enable() { }

    protected void onMobDamagePlayer(Player player, Mob mob, Projectile projectile,
                                     EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (projectile != null) return;
        if (mob.getFireTicks() <= 0) return;
        event.setDamage(event.getFinalDamage() * 0.5);
    }
}
