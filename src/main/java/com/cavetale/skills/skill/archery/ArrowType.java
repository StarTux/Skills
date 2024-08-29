package com.cavetale.skills.skill.archery;

import com.cavetale.worldmarker.util.Tags;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.inventory.ItemStack;

/**
 * Arrows marked by this plugin.  They are treated differently
 * depending on the situation.  Some are mutually exclusive.  Some are
 * optional
 */
public enum ArrowType {
    PRIMARY("skills:primary_arrow"), // Shot by player
    BONUS("skills:bonus_arrow"), // Shot by talent
    SPAM("skills:spam_arrow"), // Shot by talent
    HAIL("skills:hail_arrow"), // Do not damage shooter
    NO_PICKUP("skills:no_pickup"), // Spam arrow because vanilla betrays us
    ;

    private final NamespacedKey namespacedKey;

    ArrowType(final String key) {
        this.namespacedKey = NamespacedKey.fromString(key);
    }

    public boolean is(AbstractArrow arrow) {
        return arrow.getPersistentDataContainer().has(namespacedKey)
            || is(arrow.getItemStack());
    }

    public void set(AbstractArrow arrow) {
        Tags.set(arrow.getPersistentDataContainer(), namespacedKey, (byte) 1);
    }

    public void set(ItemStack arrow) {
        arrow.editMeta(meta -> {
                Tags.set(meta.getPersistentDataContainer(), namespacedKey, (byte) 1);
            });
    }

    public boolean is(ItemStack arrow) {
        return arrow.getItemMeta().getPersistentDataContainer().has(namespacedKey);
    }

    /**
     * Set the state if not already present.
     * @true if the state was set, false otherwise
     */
    public boolean getOrSet(AbstractArrow arrow) {
        if (is(arrow)) return false;
        set(arrow);
        return true;
    }
}
