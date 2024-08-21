package com.cavetale.skills.skill.archery;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.archerySkill;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class BonusArrowTalent extends Talent {
    public BonusArrowTalent() {
        super(TalentType.BONUS_ARROW, "Legolas",
              "Fully charged bow hits trigger another free shot",
              "As soon as a fully charged :arrow:arrow hits a mob, you launch another free :arrow:arrow. The additional :arrow:arrow is shot in the direction you are looking and may trigger yet another arrow.",
              "Your bow must be in your main hand.");
        addLevel(4, "2 extra arrows");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.GOLDEN_QUIVER);
    }

    protected void onBowDamage(Player player, AbstractArrow arrow, Mob mob) {
        if (!isPlayerEnabled(player)) return;
        if (!arrow.isCritical()) return;
        final boolean primary = ArrowType.PRIMARY.is(arrow);
        if (!primary && !ArrowType.BONUS.is(arrow)) return;
        Session session = sessionOf(player);
        if (session.archery.isBonusArrowFiring()) return;
        final ItemStack bow = player.getInventory().getItemInMainHand();
        if (bow.getType() != Material.BOW) return;
        if (primary) {
            session.archery.setBonusArrowCount(1);
        } else {
            final int bonusArrowCount = session.archery.getBonusArrowCount();
            if (bonusArrowCount >= 2) return;
            session.archery.setBonusArrowCount(bonusArrowCount + 1);
        }
        final int power = bow.getEnchantmentLevel(Enchantment.POWER);
        session.archery.setBonusArrowFiring(true);
        Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> {
                session.archery.setBonusArrowFiring(false);
                if (!player.isOnline()) return;
                if (player.getInventory().getItemInMainHand().getType() != Material.BOW) return;
                final Arrow bonusArrow = player.launchProjectile(Arrow.class);
                if (bonusArrow == null) return;
                ArrowType.BONUS.set(bonusArrow);
                ArrowType.NO_PICKUP.set(bonusArrow);
                bonusArrow.setCritical(true);
                bonusArrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                // Formula from https://minecraft.wiki/w/Arrow#Damage
                bonusArrow.setDamage(power > 0
                                     ? 2.5 + (double) power * 0.5
                                     : 2.0);
                bonusArrow.setVelocity(bonusArrow.getVelocity().normalize().multiply(3.0));
                archerySkill().onShootBow(player, bonusArrow);
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, SoundCategory.MASTER, 0.2f, 1.5f);
                if (sessionOf(player).isDebugMode()) {
                    player.sendMessage(talentType + "!");
                }
            }, 8L);
    }
}
