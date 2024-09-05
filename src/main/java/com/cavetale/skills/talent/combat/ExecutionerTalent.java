package com.cavetale.skills.talent.combat;

import com.cavetale.mytems.event.combat.DamageCalculationEvent;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ExecutionerTalent extends Talent {
    public ExecutionerTalent() {
        super(TalentType.EXECUTIONER, "Executioner",
              "Fully charged axe attacks kill mobs low on health");
        addLevel(3, "Instantly kill mobs under 10% health");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.IRON_AXE);
    }

    public void onDamageCalculation(Player player, DamageCalculationEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (player.getAttackCooldown() < 1.0) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !MaterialTags.AXES.isTagged(item.getType())) return;
        final double health = event.getTarget().getHealth();
        if (health < 0.01) return;
        final double maxHealth = event.getTarget().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (maxHealth < 0.01) return;
        if (health / maxHealth > 0.1) return;
        event.getCalculation().getOrCreateFinalDamageModifier().addFlatDamage(health, "skills:executioner");
    }
}
