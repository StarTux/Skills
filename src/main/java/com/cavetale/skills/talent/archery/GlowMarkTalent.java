package com.cavetale.skills.talent.archery;

import com.cavetale.mytems.event.combat.DamageCalculationEvent;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public final class GlowMarkTalent extends Talent {
    public GlowMarkTalent() {
        super(TalentType.GLOW_MARK, "Mark",
              "Glowing enemies take more arrow damage",
              "When your :arrow:arrow hits an enemy with the :glowing_effect:Glowing Potion Effect, the arrow damage will be doubled.");
        addLevel(1, "Glowing enemies take +" + levelToPercentage(1) + "% arrow damage");
        addLevel(1, "Glowing enemies take +" + levelToPercentage(2) + "% arrow damage");
        addLevel(1, "Glowing enemies take +" + levelToPercentage(3) + "% arrow damage");
        addLevel(1, "Glowing enemies take +" + levelToPercentage(4) + "% arrow damage");
        addLevel(1, "Glowing enemies take +" + levelToPercentage(5) + "% arrow damage");
    }

    private static int levelToPercentage(int level) {
        return level * 10;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.GLOW_BERRIES);
    }

    public void onPlayerDamageEntityCalculation(Player player, AbstractArrow arrow, LivingEntity target, DamageCalculationEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (!target.hasPotionEffect(PotionEffectType.GLOWING)) return;
        final int level = getTalentLevel(player);
        if (level < 1) return;
        final int percentage = levelToPercentage(level);
        event.getCalculation().getOrCreateBaseDamageModifier().addFactorBonus(percentage * 0.01, "skills:glow_mark");
        event.setHandled(true);
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + "");
        }
    }
}
