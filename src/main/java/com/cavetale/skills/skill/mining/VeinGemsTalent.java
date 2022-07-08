package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class VeinGemsTalent extends Talent {
    protected VeinGemsTalent() {
        super(TalentType.VEIN_GEMS);
    }

    @Override
    public String getDisplayName() {
        return "Gem Vein Mining";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Mining gem ores will attempt to break the entire vein",
                       "Works on"
                       + " :diamond_ore:Diamond,"
                       + " :emerald_ore:Emerald, and"
                       + " :nether_quartz_ore:Nether Quartz."
                       + " Requires Efficiency on your pickaxe."
                       + " Each Efficiency level breaks 5 blocks at once."
                       + "\n\nMine without this feature by sneaking.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND_PICKAXE);
    }
}
