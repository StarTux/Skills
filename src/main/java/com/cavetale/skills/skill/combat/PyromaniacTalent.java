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

public final class PyromaniacTalent extends Talent {
    protected PyromaniacTalent() {
        super(TalentType.PYROMANIAC);
    }

    @Override
    public String getDisplayName() {
        return "Pyromaniac";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Monsters set on fire take +30% damage");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.CAMPFIRE);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile, EntityDamageByEntityEvent event) {
        if (projectile != null) return;
        if (!isPlayerEnabled(player)) return;
        if (mob.getFireTicks() <= 0) return;
        event.setDamage(event.getFinalDamage() * 1.3);
    }
}
