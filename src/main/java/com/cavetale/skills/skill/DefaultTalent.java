package com.cavetale.skills.skill;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class DefaultTalent extends Talent {
    DefaultTalent(final TalentType type) {
        super(type, "Default", "Empty Text");
        addLevel(1, "Empty Text");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }
};
