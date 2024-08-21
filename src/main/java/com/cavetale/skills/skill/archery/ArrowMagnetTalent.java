package com.cavetale.skills.skill.archery;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.inventory.ItemStack;

public final class ArrowMagnetTalent extends Talent {
    public ArrowMagnetTalent() {
        super(TalentType.ARROW_MAGNET, "Arrow Magnet",
              "Drops from mobs killed by arrow are warped to you",
              "When you shoot and kill a mob, its drops and exp will land at your feet for you to collect");
        addLevel(1, "Pick up mob drops");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.MAGNET);
    }
};
