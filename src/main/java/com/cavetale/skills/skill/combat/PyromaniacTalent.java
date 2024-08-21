package com.cavetale.skills.skill.combat;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class PyromaniacTalent extends Talent {
    protected PyromaniacTalent() {
        super(TalentType.PYROMANIAC, "Pyromaniac",
              "Monsters set on fire take more damage");
        addLevel(2, "+30% damage to monsters set on fire");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.CAMPFIRE);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (mob.getFireTicks() <= 0) return;
        event.setDamage(event.getFinalDamage() * 1.3);
    }
}
