package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public final class IronAgeTalent extends Talent {
    protected final CombatSkill combatSkill;

    protected IronAgeTalent(final SkillsPlugin plugin, final CombatSkill combatSkill) {
        super(plugin, TalentType.IRON_AGE);
        this.combatSkill = combatSkill;
        this.description = "Iron weapons deal +1 base damage";
        this.infoPages = List.of();
    }

    @Override protected void enable() { }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile,
                                     EntityDamageByEntityEvent event) {
        if (projectile != null) return;
        if (!isPlayerEnabled(player)) return;
        if (item.getType() != Material.IRON_SWORD && item.getType() != Material.IRON_AXE) return; // Check for dealing damage with an iron weapon
        event.setDamage(event.getDamage() + 1); // +1 base damage before any damage reduction
    }
}
