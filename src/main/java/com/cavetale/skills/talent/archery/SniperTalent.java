package com.cavetale.skills.talent.archery;

import com.cavetale.mytems.event.combat.DamageCalculationEvent;
import com.cavetale.skills.skill.archery.ArrowType;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class SniperTalent extends Talent {
    public SniperTalent() {
        super(TalentType.SNIPER, "Sniper",
              "Arrows from a fully charged bow receive bonus damage for every block they travel.",
              "When your arrow hits its target, it will have picked up one additional damage for every block distance from you.");
        addLevel(1, levelToPercentage(1) + "% damage of blocks travelled");
        addLevel(1, levelToPercentage(2) + "% damage of blocks travelled");
        addLevel(1, levelToPercentage(3) + "% damage of blocks travelled");
    }

    private static int levelToPercentage(int level) {
        return level;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.SPYGLASS);
    }

    public void onPlayerDamageEntityCalculation(Player player, AbstractArrow arrow, LivingEntity target, DamageCalculationEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (!arrow.isCritical() || arrow.isShotFromCrossbow()) return;
        if (!ArrowType.PRIMARY.is(arrow) && !ArrowType.BONUS.is(arrow)) return;
        final int level = getTalentLevel(player);
        if (level < 1) return;
        // Get distance
        final Location a = arrow.getOrigin();
        if (a == null) return;
        final Location b = arrow.getLocation();
        if (!a.getWorld().equals(b.getWorld())) return;
        final double distance = a.distance(b);
        if (Double.isNaN(distance) || Double.isInfinite(distance) || distance <= 0.0) return;
        // Compute bonus
        final int percentage = levelToPercentage(level);
        final double bonus = distance * (double) percentage * 0.01;
        event.getCalculation().getOrCreateBaseDamageModifier().addFlatBonus(bonus, "skills:sniper");
        event.setHandled(true);
        if (isDebugTalent(player)) {
            player.sendMessage(talentType
                               + " level:" + level
                               + " %:" + percentage
                               + " distance:" + String.format("%.2f", distance)
                               + " = bonus:" + String.format("%.2f", bonus));
        }
    }
};
