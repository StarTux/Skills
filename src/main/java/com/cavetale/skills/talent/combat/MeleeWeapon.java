package com.cavetale.skills.talent.combat;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class MeleeWeapon {
    public static boolean isMeleeWeapon(ItemStack itemStack) {
        if (itemStack == null) return false;
        final Material mat = itemStack.getType();
        return Tag.ITEMS_AXES.isTagged(mat)
            || Tag.ITEMS_SWORDS.isTagged(mat)
            || mat == Material.MACE;
    }

    public static boolean hasMeleeWeapon(Player player) {
        return isMeleeWeapon(player.getInventory().getItemInMainHand());
    }

    private MeleeWeapon() { }
}
