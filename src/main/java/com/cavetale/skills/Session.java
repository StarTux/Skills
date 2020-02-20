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
import org.bukkit.entity.Player;

final class Session {
    final SkillsPlugin plugin;
    final UUID uuid;
    SQLPlayer playerRow;
    EnumMap<SkillType, SQLSkill> skillRows = new EnumMap<>(SkillType.class);
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
    ProgressBar skillBar;
    int noSave = 0;
    int tick;

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
        skillBar = new ProgressBar("skills", BarColor.WHITE, BarStyle.SEGMENTED_20);
        skillBar.add(player);
        skillBar.hide();
    }

    void onDisable() {
        skillBar.clear();
        skillBar.hide();
    }

    void setSkillBarTitle(SkillType skill, int level) {
        skillBar.setTitle(ChatColor.GRAY + skill.displayName + " level " + level);
    }

    void showSkillBar(@NonNull Player player, @NonNull SkillType skill,
                      final double oldProg, final double newProg,
                      final int oldLevel, final int newLevel,
                      final boolean levelup) {
        if (!skillBar.isAlive() || skillBar.skill != skill) {
            skillBar.setProgress(oldProg);
            skillBar.skill = skill;
        }
        final int timer = 10;
        if (levelup) {
            setSkillBarTitle(skill, oldLevel);
            skillBar.animateProgress(1.0, timer);
            skillBar.setPostAnimation(() -> {
                    setSkillBarTitle(skill, newLevel);
                    skillBar.setProgress(0.0);
                    if (newProg > 0.0) {
                        skillBar.animateProgress(newProg, timer);
                    }
                });
        } else if (newProg < skillBar.getProgress()) {
            if (skillBar.isAnimating()) {
                skillBar.setPostAnimation(() -> {
                        setSkillBarTitle(skill, newLevel);
                        skillBar.setProgress(0.0);
                        if (newProg > 0.0) {
                            skillBar.animateProgress(newProg, timer);
                        }
                    });
            } else {
                // Should not happen
                plugin.getLogger().warning("Session: The improbable just happened.");
                setSkillBarTitle(skill, newLevel);
                skillBar.setProgress(newProg);
            }
        } else {
            setSkillBarTitle(skill, oldLevel);
            skillBar.animateProgress(newProg, timer);
        }
        skillBar.setLifespan(200);
    }

    void onTick() {
        tick += 1;
        if (immortal > 0) immortal -= 1;
        if (archerZone > 0) {
            archerZone -= 1;
            if (archerZone == 0) archerZoneKills = 0;
        }
        if (skillBar.skill != null && skillBar.isAlive()) {
            if (!skillBar.tick()) {
                skillBar.skill = null;
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
