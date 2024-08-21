package com.cavetale.skills.skill.combat;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class IronAgeTalent extends Talent {
    protected IronAgeTalent() {
        super(TalentType.IRON_AGE, "Iron Age",
              "Iron weapons more base damage",
              "Iron weapons are Iron Sword and Iron Axe");
        addLevel(1, "+1 base damage");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.IRON_SWORD);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        // Check for dealing damage with an iron weapon
        if (item.getType() != Material.IRON_SWORD && item.getType() != Material.IRON_AXE) return;
        // +1 base damage before any damage reduction
        event.setDamage(event.getDamage() + 1);
    }
}
