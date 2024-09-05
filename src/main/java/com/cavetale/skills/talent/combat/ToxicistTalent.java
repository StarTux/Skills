package com.cavetale.skills.talent.combat;

import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public final class ToxicistTalent extends Talent {
    public ToxicistTalent() {
        super(TalentType.TOXICIST, "Toxicist",
              "Bane of Arthropods deals extra damage against poisoned mobs");
        addLevel(2, "+1 damage");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.POISONOUS_POTATO);
    }

    public void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (item == null || item.getType() == Material.ENCHANTED_BOOK || item.getEnchantmentLevel(Enchantment.BANE_OF_ARTHROPODS) <= 0
            || player.getAttackCooldown() != 1.0 || !mob.hasPotionEffect(PotionEffectType.POISON)) return;
        event.setDamage(event.getDamage() + (item.getEnchantmentLevel(Enchantment.BANE_OF_ARTHROPODS) + 1) / 2);
    }
}
