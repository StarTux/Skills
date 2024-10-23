package com.cavetale.skills.talent.archery;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class ArrowSpeedTalent extends Talent {
    public ArrowSpeedTalent() {
        super(TalentType.ARROW_SPEED, "Dart Swiftness",
              "Your extra :speed_effect:movement speed is added to bow :arrow:arrow speed",
              "Your :speed_effect:movement speed increase by the potions or certain :sneakers:equipment, will be added to the velocity of your :arrow:bow arrows, increasing their damage.");
        addLevel(1, "Add the movement speed");
        addLevel(1, "Add " + 2 + " times the movement speed");
        addLevel(1, "Add " + 3 + " times the movement speed");
        addLevel(1, "Add " + 4 + " times the movement speed");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.MOUSE_CURSOR);
    }

    public void onShootBow(Player player, AbstractArrow arrow) {
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
            player.sendMessage(talentType
                               + " lvl:" + level
                               + " factor:" + factor
                               + " speed:" + String.format("%.02f", extraSpeed)
                               + " bonus:" + String.format("%.02f", bonus));
        }
    }
}
