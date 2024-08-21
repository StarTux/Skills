package com.cavetale.skills.skill.archery;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class GlowMarkTalent extends Talent {
    public GlowMarkTalent() {
        super(TalentType.GLOW_MARK, "Mark",
              "Glowing enemies take mroearrow damage",
              "When your :arrow:arrow hits an enemy with the :glowing_effect:Glowing Potion Effect, the arrow damage will be doubled.");
        addLevel(4, "Glowing enemies take double arrow damage");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.GLOW_BERRIES);
    }

    protected void onArrowCollide(Player player, AbstractArrow arrow, LivingEntity target) {
        if (!isPlayerEnabled(player)) return;
        if (!target.hasPotionEffect(PotionEffectType.GLOWING)) return;
        if (!ArrowType.MARK.getOrSet(arrow)) return;
        arrow.setDamage(arrow.getDamage() * 2.0);
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " dmg:" + arrow.getDamage());
        }
    }
}
