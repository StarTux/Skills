package com.cavetale.skills.talent;

import java.util.List;
import java.util.function.Supplier;
import lombok.Value;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

@Value
public final class TalentLevel {
    protected final int level;
    protected final int talentPointCost;
    protected final Supplier<ItemStack> iconSupplier;
    protected final List<String> rawDescription;
    protected final List<Component> description;

    public ItemStack createIcon() {
        return iconSupplier.get();
    }
}
