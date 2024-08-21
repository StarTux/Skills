package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class VeinGemsTalent extends Talent {
    protected VeinGemsTalent() {
        super(TalentType.VEIN_GEMS, "Gem Vein Mining",
              "Mining gem ores will attempt to break the entire vein",
              "Works on :diamond_ore:Diamond, :emerald_ore:Emerald, and :nether_quartz_ore:Nether Quartz. Requires Efficiency on your pickaxe. Each Efficiency level breaks 5 blocks at once.",
              "Mine without this feature by sneaking.");
        addLevel(2, "REMOVE");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND_PICKAXE);
    }
}
