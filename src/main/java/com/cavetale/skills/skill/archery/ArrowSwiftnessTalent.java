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
        return List.of("Your movement speed is added to bow damage",
                       ":speed_effect:Movement speed, increased by the Swiftness :potion:Potion,"
                       + " :sneakers:Sneakers or certain item sets, will be added to"
                       + " your base :bow:bow damage."
                       + "\n\nThe added movement speed is multiplied by 5.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.SNEAKERS);
    }

    protected void onShootBow(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        double bonus = 5.0 * player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
        arrow.setDamage(arrow.getDamage() + bonus);
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " +" + bonus);
        }
    }
};
