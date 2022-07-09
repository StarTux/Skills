package com.cavetale.skills.skill;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

final class DefaultTalent extends Talent {
    DefaultTalent(final TalentType type) {
        super(type);
    }

    @Override
    public String getDisplayName() {
        return talentType.name();
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("???", "" + talentType.isEnabled());
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND);
    }
};
