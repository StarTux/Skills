package com.cavetale.skills.skill.combat;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class SearingTalent extends Talent {
    protected SearingTalent() {
        super(TalentType.SEARING);
    }

    @Override
    public String getDisplayName() {
        return "Searing";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Monsters set on fire deal -30% melee damage");
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
