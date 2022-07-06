package com.cavetale.skills.skill.combat;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

public final class ToxicFurorTalent extends Talent {
    protected ToxicFurorTalent() {
        super(TalentType.TOXIC_FUROR);
    }

    @Override
    public String getDisplayName() {
        return "Toxic Furor";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Deal extra damage while you are affected"
                       + " by Poison, Wither or Nausea",
                       "You deal +1 damage for every level of"
                       + " each of the listed effects");
    }

    @Override
    public ItemStack createIcon() {
        ItemStack icon = new ItemStack(Material.POTION);
        icon.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.values());
                if (meta instanceof PotionMeta potionMeta) {
                    potionMeta.setColor(Color.GREEN);
                }
            });
        return icon;
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (item == null || player.getAttackCooldown() != 1.0) return;
        if (!player.hasPotionEffect(PotionEffectType.POISON) && !player.hasPotionEffect(PotionEffectType.WITHER)
&& !player.hasPotionEffect(PotionEffectType.CONFUSION)) return;
        int extraDamage = 0;
        if (player.hasPotionEffect(PotionEffectType.POISON)) extraDamage += player.getPotionEffect(PotionEffectType.POISON).getAmplifier();
        if (player.hasPotionEffect(PotionEffectType.WITHER)) extraDamage += player.getPotionEffect(PotionEffectType.WITHER).getAmplifier();
        if (player.hasPotionEffect(PotionEffectType.CONFUSION)) extraDamage += player.getPotionEffect(PotionEffectType.CONFUSION).getAmplifier();
        event.setDamage(event.getDamage() + extraDamage);
    }
}
