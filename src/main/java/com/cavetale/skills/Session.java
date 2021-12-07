package com.cavetale.skills;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Session {
    protected final SkillsPlugin plugin;
    protected final UUID uuid;
    protected SQLPlayer playerColumn;
    protected EnumMap<SkillType, SQLSkill> skillColumns = new EnumMap<>(SkillType.class);
    protected BossBar skillBar;
    protected SkillType shownSkill = null;
    protected int skillBarCountdown;
    protected boolean xrayActive;
    protected Tag tag;
    protected Set<Talent> talents = new HashSet<>();
    protected Set<Talent> disabledTalents = new HashSet<>();
    // Status effects, ticks remaining
    protected int immortal = 0;
    protected int archerZone = 0;
    protected int archerZoneKills = 0;
    protected boolean poisonFreebie = false;
    protected boolean noParticles = false;
    //
    protected int noSave = 0;
    protected int tick;
    protected int actionSP;
    //
    protected boolean talentsDisabled;

    protected static final class Tag {
        protected Set<String> talents;
        protected transient boolean modified;

        protected void init() {
            if (talents == null) talents = new HashSet<>();
        }
    }

    public Session(@NonNull final SkillsPlugin plugin,
            @NonNull final Player player,
            @NonNull final SQLPlayer playerColumn,
            @NonNull final Map<SkillType, SQLSkill> inSkillColumns) {
        this.plugin = plugin;
        this.uuid = player.getUniqueId();
        this.playerColumn = playerColumn;
        if (playerColumn.json != null) {
            tag = plugin.gson.fromJson(playerColumn.json, Tag.class);
        } else {
            tag = new Tag();
        }
        tag.init();
        tag.talents.stream().map(Talent::of).filter(Objects::nonNull).forEach(talents::add);
        this.skillColumns.putAll(inSkillColumns);
        skillBar = BossBar.bossBar(Component.text("Skills"),
                                   1.0f,
                                   BossBar.Color.BLUE,
                                   BossBar.Overlay.NOTCHED_10);
    }

    public void onDisable() {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.hideBossBar(skillBar);
        }
    }

    public void showSkillBar(@NonNull Player player, @NonNull SkillType skill, final int level,
                      final int points, final int totalPoints, final int newPoints) {
        if (shownSkill == skill) {
            actionSP += newPoints;
        } else {
            actionSP = newPoints;
        }
        player.sendActionBar(Component.join(JoinConfiguration.noSeparators(),
                                            Component.text("+", NamedTextColor.GRAY),
                                            Component.text(actionSP, NamedTextColor.GOLD, TextDecoration.BOLD),
                                            Component.text("SP", NamedTextColor.GRAY)));
        skillBar.name(Component.join(JoinConfiguration.noSeparators(),
                                     Component.text(skill.displayName, NamedTextColor.GOLD),
                                     Component.text(" Level ", NamedTextColor.WHITE),
                                     Component.text(level + " ", NamedTextColor.GOLD, TextDecoration.BOLD),
                                     Component.text(points, NamedTextColor.WHITE),
                                     Component.text("/", NamedTextColor.DARK_GRAY),
                                     Component.text(totalPoints, NamedTextColor.WHITE)));
        skillBar.progress((float) points / (float) totalPoints);
        shownSkill = skill;
        skillBarCountdown = 100;
        player.showBossBar(skillBar);
    }

    public void onTick() {
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
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.hideBossBar(skillBar);
                }
            }
        }
        if (noSave++ > 200) saveData();
    }

    public void saveData() {
        noSave = 0;
        if (playerColumn.modified || tag.modified) {
            if (tag.modified) {
                tag.modified = false;
                tag.talents = talents.stream().map(t -> t.key).collect(Collectors.toSet());
                playerColumn.json = plugin.gson.toJson(tag);
            }
            playerColumn.modified = false;
            plugin.saveSQL(playerColumn);
        }
        for (SQLSkill col : skillColumns.values()) {
            if (!col.modified) continue;
            col.modified = false;
            plugin.saveSQL(col);
        }
    }

    public boolean isTalentEnabled(Talent talent) {
        return !talentsDisabled && talents.contains(talent) && !disabledTalents.contains(talent);
    }

    public boolean hasTalent(@NonNull Talent talent) {
        return talents.contains(talent);
    }

    public boolean canAccessTalent(@NonNull Talent talent) {
        return talent.depends == null
            || talents.contains(talent.depends);
    }

    public int getTalentCost() {
        return talents.size() + 1;
    }

    public int getTalentPoints() {
        return playerColumn.talentPoints;
    }

    public int getLevel(SkillType skill) {
        return skillColumns.get(skill).level;
    }

    public int getSkillPoints(SkillType skill) {
        return skillColumns.get(skill).points;
    }

    public int getExpBonus(SkillType skill) {
        int level = getLevel(skill);
        return SkillsPlugin.expBonusForLevel(level);
    }
}
