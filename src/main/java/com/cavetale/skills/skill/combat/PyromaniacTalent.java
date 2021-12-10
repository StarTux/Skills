package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class PyromaniacTalent extends Talent {
    protected final CombatSkill combatSkill;

    protected PyromaniacTalent(final SkillsPlugin plugin, final CombatSkill combatSkill) {
        super(plugin, TalentType.COMBAT_FIRE);
        this.combatSkill = combatSkill;
    }

    @Override
    protected void enable() { }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile,
                                     EntityDamageByEntityEvent event) {
        if (projectile != null) return;
        if (!isPlayerEnabled(player)) return;
        if (mob.getFireTicks() <= 0) return;
        event.setDamage(event.getFinalDamage() * 1.5);
    }

    protected void onMobDamagePlayer(Player player, Mob mob, Projectile projectile,
                                     EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (projectile != null) return;
        if (mob.getFireTicks() <= 0) return;
        event.setDamage(event.getFinalDamage() * 0.5);
    }
}
