package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class VeinMetalsTalent extends Talent {
    protected VeinMetalsTalent() {
        super(TalentType.VEIN_METALS);
    }

    @Override
    public String getDisplayName() {
        return "Vein Mining - Metals";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Mining certain ores will attempt to break the entire vein",
                       "Works on Ancient Debris, Copper, Iron and Gold Ores"
                       + "Requires the Efficiency enchantment on your pickaxe.",
                       "Mine without this feature by sneaking.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.NETHERITE_PICKAXE);
    }
}
