package com.cavetale.skills;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

final class Session {
    final SkillsPlugin plugin;
    final UUID uuid;
    SQLPlayer playerRow;
    EnumMap<SkillType, SQLSkill> skillRows = new EnumMap<>(SkillType.class);
    BossBar skillBar;
    SkillType shownSkill = null;
    int skillBarCountdown;
    boolean xrayActive;
    Tag tag;
    Set<Talent> talents = new HashSet<>();
    // Status effects, ticks remaining
    int immortal = 0;
    int archerZone = 0;
    int archerZoneKills = 0;
    boolean poisonFreebie = false;
    boolean noParticles = false;
    //
    int noSave = 0;
    int tick;
    int actionSP;

    static final class Tag {
        Set<String> talents = new HashSet<>();
        transient boolean modified;
    }

    Session(@NonNull final SkillsPlugin plugin,
            @NonNull final Player player,
            @NonNull final SQLPlayer playerRow,
            @NonNull final Map<SkillType, SQLSkill> inSkillRows) {
        this.plugin = plugin;
        this.uuid = player.getUniqueId();
        this.playerRow = playerRow;
        tag = plugin.json.deserialize(playerRow.json, Tag.class, Tag::new);
        tag.talents.stream().map(Talent::of).filter(Objects::nonNull).forEach(talents::add);
        this.skillRows.putAll(inSkillRows);
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

    void showSkillBar(@NonNull Player player, @NonNull SkillType skill, final int level,
                      final int points, final int totalPoints, final int newPoints) {
        if (shownSkill == skill) {
            actionSP += newPoints;
        } else {
            actionSP = newPoints;
        }
        player.sendActionBar(ChatColor.GRAY + "+"
                             + ChatColor.GOLD + ChatColor.BOLD + actionSP
                             + ChatColor.GRAY + "SP");
        skillBar.setTitle(ChatColor.GOLD + skill.displayName
                          + ChatColor.WHITE + " Level "
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
        tick += 1;
        if (immortal > 0) immortal -= 1;
        if (archerZone > 0) {
            archerZone -= 1;
            if (archerZone == 0) archerZoneKills = 0;
        }
        if (shownSkill != null && skillBarCountdown > 0) {
            skillBarCountdown -= 1;
            if (skillBarCountdown == 0) {
                shownSkill = null;
                skillBar.setVisible(false);
            }
        }
        if (noSave++ > 200) saveData();
    }

    void saveData() {
        noSave = 0;
        if (playerRow.modified || tag.modified) {
            if (tag.modified) {
                tag.modified = false;
                tag.talents = talents.stream().map(t -> t.key).collect(Collectors.toSet());
                playerRow.json = plugin.json.serialize(tag);
            }
            playerRow.modified = false;
            plugin.sql.save(playerRow);
        }
        for (SQLSkill col : skillRows.values()) {
            if (!col.modified) continue;
            col.modified = false;
            plugin.sql.save(col);
        }
    }

    boolean hasTalent(@NonNull Talent talent) {
        return talents.contains(talent);
    }

    boolean canAccessTalent(@NonNull Talent talent) {
        return talent.depends == null
            || talents.contains(talent.depends);
    }

    int getTalentCost() {
        return talents.size() + 1;
    }

    int getTalentPoints() {
        return playerRow.talentPoints;
    }

    int getLevel(SkillType skill) {
        return skillRows.get(skill).level;
    }

    int getSkillPoints(SkillType skill) {
        return skillRows.get(skill).points;
    }
}
