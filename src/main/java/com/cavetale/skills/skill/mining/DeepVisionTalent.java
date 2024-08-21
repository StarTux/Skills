package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class DeepVisionTalent extends Talent {
    protected DeepVisionTalent() {
        super(TalentType.DEEP_VISION, "Super Deep Vision",
              "Super Vision sees through deepslate and tuff");
        addLevel(5, "Deepslate and Tuff");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.BLACK_STAINED_GLASS);
    }
}
