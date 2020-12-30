package com.cavetale.skills;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

public final class Session {
    final SkillsPlugin plugin;
    final UUID uuid;
    SQLPlayer playerRow;
    EnumMap<SkillType, SQLSkill> skillRows = new EnumMap<>(SkillType.class);
    @Getter @Setter boolean xrayActive;
    Set<Talent> talents = EnumSet.noneOf(Talent.class);
    // Status effects, ticks remaining
    @Getter @Setter int immortal = 0;
    @Getter @Setter int archerZone = 0;
    @Getter @Setter int archerZoneKills = 0;
    @Getter @Setter boolean poisonFreebie = false;
    @Getter @Setter boolean noParticles = false;
    //
    Map<SkillType, ProgressBar> skillBars = new EnumMap<>(SkillType.class);
    int noSave = 0;

    public Session(@NonNull final SkillsPlugin plugin,
                   @NonNull final UUID uuid,
                   @NonNull final SQLPlayer playerRow,
                   @NonNull final Map<SkillType, SQLSkill> inSkillRows) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.playerRow = playerRow;
        playerRow.unpack();
        playerRow.tag.talents.stream().map(Talent::of).filter(Objects::nonNull).forEach(talents::add);
        skillRows.putAll(inSkillRows);
        Player player = getPlayer();
        for (SkillType skillType : SkillType.values()) {
            ProgressBar skillBar = new ProgressBar(skillType, "skills." + skillType, BarColor.WHITE, BarStyle.SEGMENTED_20);
            if (player != null) skillBar.add(player);
            skillBar.hide();
            skillBars.put(skillType, skillBar);
        }
    }

    public Session(final SkillsPlugin plugin, final UUID uuid) {
        this(plugin, uuid, plugin.sql.playerRowOf(uuid), plugin.sql.skillRowsOf(uuid));
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void onDisable() {
        for (ProgressBar skillBar : skillBars.values()) {
            skillBar.clear();
            skillBar.hide();
        }
    }

    public void tick(int ticks) {
        if (immortal > 0) immortal -= 1;
        if (archerZone > 0) {
            archerZone -= 1;
            if (archerZone == 0) archerZoneKills = 0;
        }
        for (ProgressBar skillBar : skillBars.values()) {
            if (skillBar.isAlive()) skillBar.tick(ticks);
        }
        if (noSave++ > 200) saveData();
    }

    public void saveData() {
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

    public boolean hasTalent(@NonNull Talent talent) {
        return talents.contains(talent);
    }

    public boolean canAccessTalent(@NonNull Talent talent) {
        Talent depends = talent.getDepends();
        return depends == null || talents.contains(depends);
    }

    public int getTalentCost() {
        return talents.size() + 1;
    }

    public int getTalentPoints() {
        return playerRow.talentPoints;
    }

    public int getLevel(SkillType skill) {
        return skillRows.get(skill).level;
    }

    public int getSkillPoints(SkillType skill) {
        return skillRows.get(skill).points;
    }

    public ProgressBar getSkillBar(SkillType skillType) {
        return skillBars.get(skillType);
    }
}
