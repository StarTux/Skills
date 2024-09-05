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

public final class SwiftSniperTalent extends Talent {
    public SwiftSniperTalent() {
        super(TalentType.SWIFT_SNIPER, "Swift Sniper",
              "Your extra :speed_effect:movement speed is added to bow :arrow:arrow speed",
              "Your :speed_effect:movement speed increase by the potions or certain :sneakers:equipment, will be added to the velocity of your :arrows:bow arrows. Arrows deal more damage if they move faster.");
        addLevel(1, "Add the movement speed");
        addLevel(1, "Add " + 2 + " times the movement speed");
        addLevel(1, "Add " + 3 + " times the movement speed");
        addLevel(1, "Add " + 4 + " times the movement speed");
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
        final int factor = level;
        final double extraSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue()
            - player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
        if (extraSpeed < 0.01) return;
        final double bonus = (double) factor * extraSpeed;
        final Vector velocity = arrow.getVelocity().multiply(1.0 + bonus);
        arrow.setVelocity(velocity);
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + " " + factor + " x " + String.format("%.02f", extraSpeed) + " = " + String.format("%.02f", bonus));
        }
    }
};
