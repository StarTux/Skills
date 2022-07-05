package com.cavetale.skills.skill.combat;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class IronAgeTalent extends Talent {
    protected IronAgeTalent() {
        super(TalentType.IRON_AGE);
    }

    @Override
    public String getDisplayName() {
        return "Iron Age";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Iron weapons deal +1 base damage",
                       "Iron weapons are Iron Sword and Iron Axe");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.IRON_SWORD);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile, EntityDamageByEntityEvent event) {
        if (projectile != null) return;
        if (!isPlayerEnabled(player)) return;
        // Check for dealing damage with an iron weapon
        if (item.getType() != Material.IRON_SWORD && item.getType() != Material.IRON_AXE) return;
        // +1 base damage before any damage reduction
        event.setDamage(event.getDamage() + 1);
    }
}
