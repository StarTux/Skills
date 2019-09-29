package com.cavetale.skills;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

final class Session {
    final SkillsPlugin plugin;
    final UUID uuid;
    SQLPlayer playerColumn;
    EnumMap<SkillType, SQLSkill> skillColumns = new EnumMap<>(SkillType.class);
    BossBar skillBar;
    SkillType shownSkill = null;
    int skillBarCountdown;
    int noSave = 0;

    Session(@NonNull final SkillsPlugin plugin,
            @NonNull final Player player,
            @NonNull final SQLPlayer playerColumn,
            @NonNull final Map<SkillType, SQLSkill> inSkillColumns) {
        this.plugin = plugin;
        this.uuid = player.getUniqueId();
        this.playerColumn = playerColumn;
        this.skillColumns.putAll(inSkillColumns);
        skillBar = plugin.getServer().createBossBar("skills",
                                                    BarColor.BLUE,
                                                    BarStyle.SEGMENTED_10);
        skillBar.setVisible(false);
        skillBar.addPlayer(player);
    }

    void onDisable() {
        skillBar.removeAll();
        skillBar.setVisible(false);
    }

    void showSkillBar(@NonNull SkillType skill, final int level,
                      final int points, final int totalPoints) {
        skillBar.setTitle(ChatColor.GOLD + skill.displayName
                          + ChatColor.DARK_GRAY + " Level "
                          + ChatColor.GOLD + ChatColor.BOLD + level + " "
                          + ChatColor.WHITE + points
                          + ChatColor.DARK_GRAY + "/"
                          + ChatColor.WHITE + totalPoints);
        skillBar.setProgress((double) points / (double) totalPoints);
        shownSkill = skill;
        skillBarCountdown = 100;
        skillBar.setVisible(true);
    }

    void onTick() {
        if (shownSkill != null && skillBarCountdown > 0) {
            skillBarCountdown -= 1;
            if (skillBarCountdown == 0) {
                shownSkill = null;
                skillBar.setVisible(false);
            }
        }
        if (noSave++ > 200) {
            noSave = 0;
            saveData();
        }
    }

    void saveData() {
        if (plugin.isEnabled()) {
            plugin.database.saveAsync(playerColumn, null);
        } else {
            plugin.database.save(playerColumn);
        }
        for (SQLSkill col : skillColumns.values()) {
            if (!col.modified) continue;
            col.modified = false;
            if (plugin.isEnabled()) {
                plugin.database.saveAsync(col, null);
            } else {
                plugin.database.save(col);
            }
        }
    }
}
