package com.cavetale.skills.skill.combat;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class ExecutionerTalent extends Talent {
    protected ExecutionerTalent() {
        super(TalentType.EXECUTIONER, "Executioner",
              "Fully charged axe attacks kill mobs low on health");
        addLevel(3, "Instantly kill mobs under 10% health");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.IRON_AXE);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (item == null
            || !MaterialTags.AXES.isTagged(item.getType())
            || (mob.getHealth() / mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) > 0.1
            || player.getAttackCooldown() != 1.0) {
            return;
        }
        event.setDamage(event.getDamage() + mob.getHealth());
    }
}
