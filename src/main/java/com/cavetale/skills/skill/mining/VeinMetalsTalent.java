package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class VeinMetalsTalent extends Talent {
    protected VeinMetalsTalent() {
        super(TalentType.VEIN_METALS, "Metal Vein Mining",
              "Mining metal ores will attempt to break the entire vein",
              "Works on :ancient_debris:Ancient Debris, :copper_ore:Copper, :iron_ore:Iron and :gold_ore:Gold Ores. Requires the Efficiency enchantment on your pickaxe. Each level of Efficiency lets you break 5 blocks at once.",
              "Mine without this feature by sneaking.");
        addLevel(2, "REMOVE");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.NETHERITE_PICKAXE);
    }
}
