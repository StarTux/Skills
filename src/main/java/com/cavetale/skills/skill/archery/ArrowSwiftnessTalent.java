package com.cavetale.skills.skill.archery;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class ArrowSwiftnessTalent extends Talent {
    public ArrowSwiftnessTalent() {
        super(TalentType.ARROW_SWIFTNESS);
    }

    @Override
    public String getDisplayName() {
        return "Swift Sniper";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Your movement speed increases arrow damage",
                       "Movement speed such as increased by the Swiftness potion,"
                       + " Sneakers or certain item sets will be added to"
                       + " your base arrow damage."
                       + "\n\nThe added movement speed is multiplied by 10.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.SNEAKERS);
    }

    protected void onShootArrow(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        if (arrow.isCritical()) {
            double bonus = 10.0 * player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
            arrow.setDamage(arrow.getDamage() + bonus);
            if (sessionOf(player).isDebugMode()) {
                player.sendMessage(talentType + " +" + bonus);
            }
        }
    }
};
