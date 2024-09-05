package com.cavetale.skills.talent.mining;

import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class DeepMiningTalent extends Talent {
    public DeepMiningTalent() {
        super(TalentType.DEEP_MINING, "Deep Strip Mining",
              "Strip Mining also works on Deepslate and Tuff");
        addLevel(2, "Deepslate and Tuff");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DEEPSLATE);
    }
}
