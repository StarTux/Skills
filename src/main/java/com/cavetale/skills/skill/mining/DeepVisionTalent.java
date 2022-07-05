package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class DeepVisionTalent extends Talent {
    protected DeepVisionTalent() {
        super(TalentType.DEEP_VISION);
    }

    @Override
    public String getDisplayName() {
        return "Super Deep Vision";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Super Vision sees through deepslate and tuff");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.SPYGLASS);
    }
}
