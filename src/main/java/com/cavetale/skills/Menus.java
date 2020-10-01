package com.cavetale.skills;

import com.cavetale.skills.util.Gui;
import com.cavetale.skills.util.Items;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public final class Menus {
    private final SkillsPlugin plugin;

    public Material materialOf(int level, Material... mats) {
        int index = (mats.length * level) / 100;
        if (index > mats.length) return mats[mats.length - 1];
        return mats[index];
    }

    public Material getMaterial(Player player, SkillType skillType, int level) {
        switch (skillType) {
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

    public ItemStack getIcon(Player player, SkillType skillType) {
        Session session = plugin.sessions.of(player);
        int level = session.getLevel(skillType);
        int sp = session.getSkillPoints(skillType);
        int spNext = plugin.getSkillPoints().forLevel(level + 1);
        return Items.of(getMaterial(player, skillType, level))
            .name(ChatColor.GOLD + skillType.displayName)
            .lore("" + ChatColor.GRAY + "Level" + ChatColor.WHITE + ": " + ChatColor.WHITE + level,
                  "" + ChatColor.GRAY + "Skill Points" + ChatColor.WHITE + ": " + sp + ChatColor.DARK_GRAY + "/" + ChatColor.WHITE + spNext)
            .hide()
            .create();
    }

    public int getGuiIndex(SkillType skillType) {
        switch (skillType) {
        case MINING: return 2 + 1 * 9;
        case FARMING: return 4 + 1 * 9;
        case COMBAT: return 6 + 1 * 9;
        default: return 0;
        }
    }

    public void openSkillsMenu(Player player) {
        Gui gui = new Gui(plugin)
            .rows(3)
            .title(ChatColor.DARK_BLUE + "Skills Mk2");
        for (SkillType skillType : SkillType.values()) {
            gui.setItem(getGuiIndex(skillType), getIcon(player, skillType), click -> {
                    Gui.click(player);
                    openSkillMenu(player, skillType);
                    return true;
                });
        }
        gui.open(player);
    }

    public void openSkillMenu(Player player, SkillType skillType) {
        Gui gui = new Gui(plugin)
            .rows(3)
            .title(ChatColor.DARK_PURPLE + skillType.displayName);
        gui.setItem(-999, null, click -> {
                Gui.click(player);
                openSkillsMenu(player);
                return true;
            });
        gui.setItem(4, 0, getIcon(player, skillType), click -> {
                return false;
            });
        gui.open(player);
    }
}
