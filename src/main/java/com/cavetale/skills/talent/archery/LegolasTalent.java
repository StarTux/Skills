package com.cavetale.skills.talent.archery;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.archery.ArrowType;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
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
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class LegolasTalent extends Talent {
    public LegolasTalent() {
        super(TalentType.LEGOLAS, "Legolas",
              "Fully charged bow hits trigger another free shot",
              "As soon as a fully charged :arrow:arrow hits a mob, you launch another free :arrow:arrow. The additional :arrow:arrow is shot in the direction you are looking and may trigger yet another arrow.",
              "Your bow must be in your main hand.");
        addLevel(1, 1 + " extra arrow");
        addLevel(1, 2 + " extra arrows");
        addLevel(1, 3 + " extra arrows");
        addLevel(1, 4 + " extra arrows");
        addLevel(1, 5 + " extra arrows");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.GOLDEN_QUIVER);
    }

    public void onBowDamage(Player player, AbstractArrow arrow, Mob mob) {
        if (!isPlayerEnabled(player)) return;
        if (!arrow.isCritical()) return;
        final boolean primary = ArrowType.PRIMARY.is(arrow);
        if (!primary && !ArrowType.BONUS.is(arrow)) return;
        final Session session = Session.of(player);
        if (session.archery.isLegolasFiring()) return;
        final ItemStack bow = player.getInventory().getItemInMainHand();
        if (bow.getType() != Material.BOW) return;
        final int level = session.getTalentLevel(talentType);
        if (level < 1) return;
        if (primary) {
            session.archery.setLegolasCount(1);
        } else {
            final int legolasCount = session.archery.getLegolasCount();
            if (legolasCount >= level) return;
            session.archery.setLegolasCount(legolasCount + 1);
        }
        final int power = bow.getEnchantmentLevel(Enchantment.POWER);
        session.archery.setLegolasFiring(true);
        Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> {
                session.archery.setLegolasFiring(false);
                if (!player.isOnline()) return;
                if (player.getInventory().getItemInMainHand().getType() != Material.BOW) return;
                final Arrow legolas = player.launchProjectile(Arrow.class);
                if (legolas == null) return;
                ArrowType.BONUS.set(legolas);
                ArrowType.NO_PICKUP.set(legolas);
                legolas.setCritical(true);
                legolas.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                // Formula from https://minecraft.wiki/w/Arrow#Damage
                legolas.setDamage(power > 0
                                     ? 2.5 + (double) power * 0.5
                                     : 2.0);
                legolas.setVelocity(legolas.getVelocity().normalize().multiply(3.0));
                archerySkill().onShootBow(player, legolas);
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, SoundCategory.MASTER, 0.2f, 1.5f);
                if (isDebugTalent(player)) {
                    player.sendMessage(talentType + "!");
                }
            }, 8L);
    }
}
