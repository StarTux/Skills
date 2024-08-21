package com.cavetale.skills.skill.combat;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public final class ToxicistTalent extends Talent {
    protected ToxicistTalent() {
        super(TalentType.TOXICIST, "Toxicist",
              "Bane of Arthropods deals extra damage against poisoned mobs");
        addLevel(2, "You deal +1 damage for every level of the Bane of Arthropods enchantment on your weapon");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.POISONOUS_POTATO);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (item == null || item.getType() == Material.ENCHANTED_BOOK || item.getEnchantmentLevel(Enchantment.BANE_OF_ARTHROPODS) <= 0
            || player.getAttackCooldown() != 1.0 || !mob.hasPotionEffect(PotionEffectType.POISON)) return;
        event.setDamage(event.getDamage() + (item.getEnchantmentLevel(Enchantment.BANE_OF_ARTHROPODS) + 1) / 2);
    }
}
