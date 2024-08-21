package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class DeepMiningTalent extends Talent {
    protected DeepMiningTalent() {
        super(TalentType.DEEP_MINING, "Deep Strip Mining",
              "Strip Mining also works on Deepslate and Tuff");
        addLevel(2, "Deepslate and Tuff");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DEEPSLATE);
    }
}
