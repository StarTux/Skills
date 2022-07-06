package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class DeepMiningTalent extends Talent {
    protected DeepMiningTalent() {
        super(TalentType.DEEP_MINING);
    }

    @Override
    public String getDisplayName() {
        return "Deep Strip Mining";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Strip Mining also works on Deepslate and Tuff");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DEEPSLATE);
    }
}
