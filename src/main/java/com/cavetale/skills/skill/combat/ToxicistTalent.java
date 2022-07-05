package com.cavetale.skills.skill.combat;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public final class ToxicistTalent extends Talent {
    protected ToxicistTalent() {
        super(TalentType.TOXICIST);
    }

    @Override
    public String getDisplayName() {
        return "Toxicist";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Bane of Arthropods deals extra damage"
                       + " against poisoned mobs",
                       "You deal +1 damage for every level of the"
                       + " Bane of Arthropods enchantment on your weapon");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.POISONOUS_POTATO);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile, EntityDamageByEntityEvent event) {
        if (projectile != null) return;
        if (!isPlayerEnabled(player)) return;
        if (item == null || item.getType() == Material.ENCHANTED_BOOK || item.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS) <= 0
            || player.getAttackCooldown() != 1.0 || !mob.hasPotionEffect(PotionEffectType.POISON)) return;
        event.setDamage(event.getDamage() + (item.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS) + 1) / 2);
    }
}
