package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class SilkMultiTalent extends Talent {
    protected SilkMultiTalent() {
        super(TalentType.SILK_MULTI);
    }

    @Override
    public String getDisplayName() {
        return "Silk Fortune";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Get more non-metallic drops from Silk Stripping",
                       "Upgrade Silk Stripping to get more drops"
                       + " from non-metallic ores. Works on everything"
                       + " but Ancient Debris, Copper, Iron and Gold Ores.",
                       "This method may yield as much reward as Fortune V"
                       + " would, but with greater variance.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }
}
