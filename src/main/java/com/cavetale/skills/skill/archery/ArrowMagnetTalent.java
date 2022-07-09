package com.cavetale.skills.skill.archery;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public final class ArrowMagnetTalent extends Talent {
    public ArrowMagnetTalent() {
        super(TalentType.ARROW_MAGNET);
    }

    @Override
    public String getDisplayName() {
        return "Arrow Magnet";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Drops from mobs killed by arrow are warped to you",
                       "When you shoot and kill a mob,"
                       + " its drops and exp will land"
                       + " at your feet for you to collect");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.MAGNET);
    }
};
