package com.cavetale.skills;

import java.util.EnumMap;
import java.util.EnumSet;
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
    Set<Talent> talents = EnumSet.noneOf(Talent.class);
    // Status effects, ticks remaining
    int immortal = 0;
    int archerZone = 0;
    int archerZoneKills = 0;
    boolean poisonFreebie = false;
    boolean noParticles = false;
    //
    Map<SkillType, ProgressBar> skillBars = new EnumMap<>(SkillType.class);
    int noSave = 0;
    int tick;

    Session(@NonNull final SkillsPlugin plugin,
            @NonNull final Player player,
            @NonNull final SQLPlayer playerRow,
            @NonNull final Map<SkillType, SQLSkill> inSkillRows) {
        this.plugin = plugin;
        this.uuid = player.getUniqueId();
        this.playerRow = playerRow;
        playerRow.unpack();
        playerRow.tag.talents.stream().map(Talent::of).filter(Objects::nonNull).forEach(talents::add);
        this.skillRows.putAll(inSkillRows);
        for (SkillType skillType : SkillType.values()) {
            ProgressBar skillBar = new ProgressBar(skillType, "skills." + skillType, BarColor.WHITE, BarStyle.SEGMENTED_20);
            skillBar.add(player);
            skillBar.hide();
            skillBars.put(skillType, skillBar);
        }
    }

    void onDisable() {
        for (ProgressBar skillBar : skillBars.values()) {
            skillBar.clear();
            skillBar.hide();
        }
    }

    void onTick() {
        tick += 1;
        if (immortal > 0) immortal -= 1;
        if (archerZone > 0) {
            archerZone -= 1;
            if (archerZone == 0) archerZoneKills = 0;
        }
        for (ProgressBar skillBar : skillBars.values()) {
            if (skillBar.isAlive()) skillBar.tick();
        }
        if (noSave++ > 200) saveData();
    }

    void saveData() {
        noSave = 0;
        if (playerRow.dirty) {
            playerRow.dirty = false;
            playerRow.tag.talents = talents.stream().map(t -> t.key).collect(Collectors.toSet());
            playerRow.pack();
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

    ProgressBar getSkillBar(SkillType skillType) {
        return skillBars.get(skillType);
    }
}
