package com.cavetale.skills.skill.archery;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class ArrowDamageTalent extends Talent {
    public ArrowDamageTalent() {
        super(TalentType.ARROW_DAMAGE);
    }

    @Override
    public String getDisplayName() {
        return "Arrow Damage";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Arrows from a fully charged bow have double damage",
                       "The base damage is doubled if you charge your bow fully.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.ARROW);
    }

    protected void onShootArrow(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        if (arrow.isCritical()) {
            arrow.setDamage(arrow.getDamage() * 2.0);
        }
        if (sessionOf(player).isDebugMode()) player.sendMessage(talentType + " => " + arrow.getDamage());
    }
};
