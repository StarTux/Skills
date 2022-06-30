package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public final class ToxicFurorTalent extends Talent {
    protected final CombatSkill combatSkill;

    protected ToxicFurorTalent(final SkillsPlugin plugin, final CombatSkill combatSkill) {
        super(plugin, TalentType.TOXIC_FUROR);
        this.combatSkill = combatSkill;
        this.description = "Deal extra damage while affected by Poison, Wither or Nausea";
        this.infoPages = List.of();
    }

    @Override protected void enable() { }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile,
                                     EntityDamageByEntityEvent event) {
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
