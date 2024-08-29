package com.cavetale.skills.skill.archery;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class ArrowSwiftnessTalent extends Talent {
    public ArrowSwiftnessTalent() {
        super(TalentType.ARROW_SWIFTNESS, "Swift Sniper",
              "Your :speed_effect:movement speed is added to bow :arrow:arrow speed",
              ":speed_effect:Movement speed, increased by the potions or certain :sneakers:equipment, will be added to the velocity of your :arrows:bow arrows. Arrows deal more damage if they move faster.");
        addLevel(1, levelToFactor(1) + " times the movement speed");
        addLevel(1, levelToFactor(2) + " times the movement speed");
        addLevel(1, levelToFactor(3) + " times the movement speed");
        addLevel(1, levelToFactor(4) + " times the movement speed");
        addLevel(1, levelToFactor(5) + " times the movement speed");
    }

    /**
     * The series goes like so.
     * 1, 3, 5, 7, 9
     */
    private static int levelToFactor(int level) {
        return level * 2 - 1;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.SNEAKERS);
    }

    protected void onShootBow(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        final Session session = Session.of(player);
        final int level = session.getTalentLevel(talentType);
        if (level < 1) return;
        final int factor = levelToFactor(level);
        final double movementSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
        final double bonus = (double) factor * movementSpeed;
        final Vector velocity = arrow.getVelocity().multiply(1.0 + bonus);
        arrow.setVelocity(velocity);
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + " " + factor + "*" + movementSpeed + " = " + bonus);
        }
    }
};
