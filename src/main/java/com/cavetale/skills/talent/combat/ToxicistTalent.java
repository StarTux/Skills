package com.cavetale.skills.talent.combat;

import com.cavetale.mytems.event.combat.DamageCalculationEvent;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public final class ToxicistTalent extends Talent {
    public ToxicistTalent() {
        super(TalentType.TOXICIST, "Toxicist",
              "Bane of Arthropods deals extra raw damage against poisoned mobs.");
        addLevel(1, "+" + levelToBonus(1) + " raw damage");
        addLevel(2, "+" + levelToBonus(2) + " raw damage");
        addLevel(3, "+" + levelToBonus(3) + " raw damage");
        addLevel(4, "+" + levelToBonus(4) + " raw damage");
    }

    private static int levelToBonus(int level) {
        return level * 2 - 1;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.POISONOUS_POTATO);
    }

    public void onDamageCalculation(Player player, DamageCalculationEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (!event.getTarget().hasPotionEffect(PotionEffectType.POISON)) return;
        final ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!MeleeWeapon.isMeleeWeapon(weapon)) return;
        if (weapon.getEnchantmentLevel(Enchantment.BANE_OF_ARTHROPODS) <= 0) return;
        final int level = getTalentLevel(player);
        if (level < 1) return;
        final int bonus = levelToBonus(level);
        event.getCalculation().getOrCreateBaseDamageModifier().addRawBonus(bonus, "skills:toxicist");
        event.setHandled(true);
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + " lvl:" + level + " bonus:" + bonus);
        }
    }
}
