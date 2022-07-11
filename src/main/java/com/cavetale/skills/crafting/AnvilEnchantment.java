package com.cavetale.skills.crafting;

import java.util.Set;
import lombok.Value;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

/**
 * This represents a single item which can be enchanted with an
 * otherwise forbidden enchant.
 */
@Value
public final class AnvilEnchantment {
    public final Material item;
    public final Enchantment enchantment;
    public final Set<Enchantment> conflicts;
}
