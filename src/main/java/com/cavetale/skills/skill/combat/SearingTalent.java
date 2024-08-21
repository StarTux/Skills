package com.cavetale.skills.skill.combat;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class SearingTalent extends Talent {
    protected SearingTalent() {
        super(TalentType.SEARING, "Searing",
              "Monsters set on fire deal less melee damage");
        addLevel(1, "-30% melee damage from mobs set on fire");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.SOUL_CAMPFIRE);
    }

    protected void onMobDamagePlayer(Player player, Mob mob, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (mob.getFireTicks() <= 0) return;
        event.setDamage(event.getFinalDamage() * 0.7);
    }
}
