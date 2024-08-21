package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class SilkMetalsTalent extends Talent {
    protected SilkMetalsTalent() {
        super(TalentType.SILK_METALS, "Silk Extraction",
              "Get more metal from Silk Stripping",
              "Upgrade Silk Stripping to get more drops from metal ores. Works on :ancient_debris:Ancient Debris, :copper_ore:Copper, :iron_ore:Iron and :gold_ore:Gold Ores.",
              "This method may yield as much reward as Fortune V would, but with greater variance.");
        addLevel(4, "More drops from metal ores");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.NETHERITE_SCRAP);
    }
}
