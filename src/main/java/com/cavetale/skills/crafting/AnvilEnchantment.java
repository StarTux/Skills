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
    public final int maxLevel;
    public final Set<Enchantment> conflicts;

    public AnvilEnchantment(final Material item, final Enchantment enchantment,
                            final int maxLevel, final Set<Enchantment> conflicts) {
        this.item = item;
        this.enchantment = enchantment;
        this.maxLevel = maxLevel;
        this.conflicts = conflicts;
    }

    public AnvilEnchantment(final Material item, final Enchantment enchantment, final Set<Enchantment> conflicts) {
        this(item, enchantment, enchantment.getMaxLevel(), conflicts);
    }

    public AnvilEnchantment(final Material item, final Enchantment enchantment, final int maxLevel) {
        this(item, enchantment, maxLevel, Set.of());
    }

    public AnvilEnchantment(final Material item, final Enchantment enchantment) {
        this(item, enchantment, enchantment.getMaxLevel(), Set.of());
    }
}
