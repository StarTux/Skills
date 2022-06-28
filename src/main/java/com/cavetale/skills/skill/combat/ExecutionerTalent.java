package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.List;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class ExecutionerTalent extends Talent {
    protected final CombatSkill combatSkill;

    protected ExecutionerTalent(final SkillsPlugin plugin, final CombatSkill combatSkill) {
        super(plugin, TalentType.EXECUTIONER);
        this.combatSkill = combatSkill;
        this.description = "Fully charged axe attacks kill mobs under 10% health";
        this.infoPages = List.of();
    }

    @Override protected void enable() { }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile,
                                     EntityDamageByEntityEvent event) {
        if (projectile != null) return;
        if (!isPlayerEnabled(player)) return;
        if (item == null || !MaterialTags.AXES.isTagged(item.getType()) || (mob.getHealth() / mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) > 0.1
|| player.getAttackCooldown() != 1.0) return;
        event.setDamage(event.getDamage() + mob.getHealth());
        mob.setHealth(1);
    }
}
