package com.cavetale.skills.talent.combat;

import com.cavetale.mytems.event.combat.DamageCalculationEvent;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ExecutionerTalent extends Talent {
    public ExecutionerTalent() {
        super(TalentType.EXECUTIONER, "Executioner",
              "Fully charged mace attacks kill mobs low on health.");
        addLevel(1, "Instantly kill mobs under " + levelToPercentage(1) + "% health");
        addLevel(1, "Instantly kill mobs under " + levelToPercentage(2) + "% health");
        addLevel(1, "Instantly kill mobs under " + levelToPercentage(3) + "% health");
    }

    private static int levelToPercentage(int level) {
        return switch (level) {
        case 1 -> 3;
        case 2 -> 6;
        case 3 -> 10;
        default -> 0;
        };
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.MACE);
    }

    public void onDamageCalculation(Player player, DamageCalculationEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (player.getAttackCooldown() < 1.0) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.MACE) return;
        final int level = getTalentLevel(player);
        if (level < 1) return;
        final int percentage = levelToPercentage(level);
        final double health = event.getTarget().getHealth();
        if (health < 0.01) return;
        final double maxHealth = event.getTarget().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (maxHealth < 0.01) return;
        if (health / maxHealth > (percentage * 0.01)) return;
        event.getCalculation().getOrCreateFinalDamageModifier().addFlatDamage(health, "skills:executioner");
    }
}
