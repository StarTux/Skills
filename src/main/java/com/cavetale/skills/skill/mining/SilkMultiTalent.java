package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class SilkMultiTalent extends Talent {
    protected SilkMultiTalent() {
        super(TalentType.SILK_MULTI, "Silk Fortune",
              "Get more non-metallic drops from Silk Stripping",
              "Upgrade Silk Stripping to get more drops from non-metallic ores. Works on everything but Ancient Debris, Copper, Iron and Gold Ores.",
              "This method may yield as much reward as Fortune V would, but with greater variance.");
        addLevel(3, "Fortune V");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.LAPIS_LAZULI);
    }
}
