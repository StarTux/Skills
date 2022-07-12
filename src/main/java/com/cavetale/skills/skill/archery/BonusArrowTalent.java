package com.cavetale.skills.skill.archery;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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
        super(TalentType.BONUS_ARROW);
    }

    @Override
    public String getDisplayName() {
        return "Legolas";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Fully charged bow hits trigger another free shot",
                       "As soon as a fully charged :arrow:arrow hits a mob,"
                       + " you launch another free :arrow:arrow."
                       + " The additional :arrow:arrow is shot in the direction you are looking"
                       + " and may trigger yet another arrow.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.GOLDEN_QUIVER);
    }

    protected void onBowDamage(Player player, AbstractArrow arrow, Mob mob) {
        if (!isPlayerEnabled(player)) return;
        if ((!arrow.isCritical() && !ArrowType.PRIMARY.is(arrow)) && !ArrowType.BONUS.is(arrow)) return;
        Session session = sessionOf(player);
        if (session.archery.isBonusArrowFiring()) return;
        session.archery.setBonusArrowFiring(true);
        Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> {
                session.archery.setBonusArrowFiring(false);
                if (!player.isOnline()) return;
                Arrow bonusArrow = player.launchProjectile(Arrow.class);
                if (bonusArrow == null) return;
                ArrowType.BONUS.set(bonusArrow);
                bonusArrow.setCritical(false);
                bonusArrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                archerySkill().onShootBow(player, arrow);
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, SoundCategory.MASTER, 0.2f, 1.5f);
                if (sessionOf(player).isDebugMode()) {
                    player.sendMessage(talentType + "!");
                }
            }, 8L);
    }
}
