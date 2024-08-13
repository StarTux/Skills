package com.cavetale.skills.skill.archery;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class ArrowVelocityTalent extends Talent {
    public ArrowVelocityTalent() {
        super(TalentType.ARROW_VELOCITY);
    }

    @Override
    public String getDisplayName() {
        return "Marksmanship";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Fully charged bows shoot arrows with twice the speed",
                       "Charge your :bow:bow fully, and your :arrow:arrows will launch with"
                       + " 150% speed. This also affects Legolas arrows.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.GILDED_WOODEN_BOW);
    }

    protected void onShootBow(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        if (!arrow.isCritical()) return;
        final Vector velocity = arrow.getVelocity().multiply(1.5);
        arrow.setVelocity(velocity);
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " " + velocity.length());
        }
    }
};
