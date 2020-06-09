package com.cavetale.skills;

import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum SkillType {
    MINING,
    COMBAT,
    FARMING;

    public final String key;
    public final String displayName;
    int guiIndex;

    SkillType() {
        key = name().toLowerCase();
        displayName = Msg.enumToCamelCase(this);
    }

    static SkillType ofKey(@NonNull String key) {
        for (SkillType s : SkillType.values()) {
            if (key.equals(s.key)) return s;
        }
        return null;
    }

    static void setup() {
        MINING.guiIndex = 2 + 1 * 9;
        FARMING.guiIndex = 4 + 1 * 9;
        COMBAT.guiIndex = 6 + 1 * 9;
    }

    Material materialOf(int level, Material... mats) {
        int index = (mats.length * level) / 100;
        if (index > mats.length) return mats[mats.length - 1];
        return mats[index];
    }

    Material getMaterial(Player player, int level) {
        switch (this) {
        case MINING: return materialOf(level, Material.WOODEN_PICKAXE,
                                       Material.STONE_PICKAXE,
                                       Material.IRON_PICKAXE,
                                       Material.GOLDEN_PICKAXE,
                                       Material.DIAMOND_PICKAXE);
        case COMBAT: return materialOf(level, Material.WOODEN_SWORD,
                                       Material.STONE_SWORD,
                                       Material.IRON_SWORD,
                                       Material.GOLDEN_SWORD,
                                       Material.DIAMOND_SWORD);
        case FARMING: return materialOf(level, Material.WOODEN_HOE,
                                        Material.STONE_HOE,
                                        Material.IRON_HOE,
                                        Material.GOLDEN_HOE,
                                        Material.DIAMOND_HOE);
        default: return Material.STICK;
        }
    }

    ItemStack getIcon(Player player) {
        Session session = SkillsPlugin.instance.sessions.of(player);
        int level = session.getLevel(this);
        int sp = session.getSkillPoints(this);
        int spNext = SkillsPlugin.instance.points.forLevel(level + 1);
        return Items.of(getMaterial(player, level))
            .name(ChatColor.GOLD + displayName)
            .lore("" + ChatColor.GRAY + "Level" + ChatColor.WHITE + ": " + ChatColor.WHITE + level,
                  "" + ChatColor.GRAY + "Skill Points" + ChatColor.WHITE + ": " + sp + ChatColor.DARK_GRAY + "/" + ChatColor.WHITE + spNext)
            .hide()
            .create();
    }
}
