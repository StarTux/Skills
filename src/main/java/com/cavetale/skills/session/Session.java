package com.cavetale.skills.session;

import com.cavetale.core.util.Json;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.util.Effects;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@Getter
public final class Session {
    protected final SkillsPlugin plugin;
    protected final UUID uuid;
    protected boolean enabled = false;
    protected SQLPlayer sqlPlayer;
    protected EnumMap<SkillType, SQLSkill> sqlSkills = new EnumMap<>(SkillType.class);
    protected BossBar skillBar;
    protected SkillType shownSkill = null;
    protected int skillBarCountdown;
    protected Tag tag;
    protected final Set<TalentType> talents = new HashSet<>();
    protected final Set<TalentType> disabledTalents = new HashSet<>();
    // Status effects, ticks remaining
    @Setter protected boolean xrayActive;
    @Setter protected int immortal = 0;
    @Setter protected int archerZone = 0;
    @Setter protected int archerZoneKills = 0;
    @Setter protected boolean poisonFreebie = false;
    @Setter protected boolean noParticles = false;
    @Setter protected SkillType talentGui = SkillType.MINING;
    //
    private int noSave = 0;
    private int tick;
    private int actionSP;
    //
    protected BukkitTask task;

    public Session(@NonNull final SkillsPlugin plugin, @NonNull final Player player) {
        this.plugin = plugin;
        this.uuid = player.getUniqueId();
    }

    public Session(@NonNull final SkillsPlugin plugin, @NonNull final UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
    }

    protected void loadSync() {
        // Load SQLPlayer
        this.sqlPlayer = plugin.database.find(SQLPlayer.class).eq("uuid", uuid).findUnique();
        if (sqlPlayer == null) {
            sqlPlayer = new SQLPlayer(uuid);
            plugin.database.insert(sqlPlayer);
        }
        this.tag = Json.deserialize(sqlPlayer.getJson(), Tag.class, Tag::new);
        tag.talents.stream().map(TalentType::of).filter(Objects::nonNull).forEach(talents::add);
        tag.disabledTalents.stream().map(TalentType::of).filter(Objects::nonNull).forEach(disabledTalents::add);
        // Load SQLSkills
        List<SQLSkill> sqlSkillRows = plugin.database.find(SQLSkill.class).eq("player", uuid).findList();
        for (SQLSkill sqlSkill : sqlSkillRows) {
            SkillType skillType = sqlSkill.getSkillType();
            if (skillType == null) {
                plugin.getLogger().warning("Invalid skill row: " + sqlSkill);
                continue;
            }
            this.sqlSkills.put(skillType, sqlSkill);
        }
        for (SkillType skillType : SkillType.values()) {
            if (!this.sqlSkills.containsKey(skillType)) {
                SQLSkill row = new SQLSkill(uuid, skillType.key);
                this.sqlSkills.put(skillType, row);
                plugin.database.insert(row);
            }
        }
    }

    protected void loadAsync(final Runnable callback) {
        plugin.database.scheduleAsyncTask(() -> {
                loadSync();
                Bukkit.getScheduler().runTask(plugin, callback);
            });
    }

    /**
     * Enable for live use.
     */
    protected void enable() {
        if (plugin.sessions.sessionsMap.get(uuid) != this) return;
        enabled = true;
        skillBar = BossBar.bossBar(Component.text("Skills"),
                                   1.0f,
                                   BossBar.Color.BLUE,
                                   BossBar.Overlay.NOTCHED_10);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
        updateAdvancements();
    }

    protected void disable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (skillBar != null) {
            Player player = getPlayer();
            if (player != null) {
                player.hideBossBar(skillBar);
            }
        }
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void updateAdvancements() {
        Player player = getPlayer();
        if (player == null) return;
        for (TalentType talent : TalentType.values()) {
            if (talents.contains(talent)) {
                plugin.advancements.give(player, talent);
            } else {
                plugin.advancements.revoke(player, talent);
            }
        }
    }

    public void showSkillBar(@NonNull Player player, @NonNull SkillType skillType, final int level,
                      final int points, final int totalPoints, final int newPoints) {
        if (shownSkill == skillType) {
            actionSP += newPoints;
        } else {
            actionSP = newPoints;
        }
        player.sendActionBar(Component.join(JoinConfiguration.noSeparators(),
                                            Component.text("+", NamedTextColor.GRAY),
                                            Component.text(actionSP, NamedTextColor.GOLD, TextDecoration.BOLD),
                                            Component.text("SP", NamedTextColor.GRAY)));
        skillBar.name(Component.join(JoinConfiguration.noSeparators(),
                                     Component.text(skillType.displayName, NamedTextColor.GOLD),
                                     Component.text(" Level ", NamedTextColor.WHITE),
                                     Component.text(level + " ", NamedTextColor.GOLD, TextDecoration.BOLD),
                                     Component.text(points, NamedTextColor.WHITE),
                                     Component.text("/", NamedTextColor.DARK_GRAY),
                                     Component.text(totalPoints, NamedTextColor.WHITE)));
        skillBar.progress((float) points / (float) totalPoints);
        shownSkill = skillType;
        skillBarCountdown = 100;
        player.showBossBar(skillBar);
    }

    public void addSkillPoints(@NonNull final SkillType skillType, final int add) {
        SQLSkill row = sqlSkills.get(skillType);
        int points = row.getPoints() + add;
        int req = SkillsPlugin.pointsForLevelUp(row.getLevel() + 1);
        if (points >= req) {
            points -= req;
            row.setLevel(row.getLevel() + 1);
            sqlPlayer.setLevels(sqlPlayer.getLevels() + 1);
            sqlPlayer.setModified(true);
            Player player = getPlayer();
            if (player != null) {
                Effects.levelup(player);
                player.showTitle(Title.title(Component.text(skillType.displayName, NamedTextColor.GOLD),
                                             Component.text("Level " + row.getLevel(), NamedTextColor.WHITE)));
            }
        }
        row.setPoints(points);
        row.setModified(true);
        Player player = getPlayer();
        if (player != null) {
            showSkillBar(player, skillType, row.getLevel(), points, req, add);
        }
    }

    public boolean rollTalentPoint(final int increase) {
        final int total = 800;
        if (talents.size() >= TalentType.COUNT) return false;
        sqlPlayer.setTalentChance(sqlPlayer.getTalentChance() + increase);
        sqlPlayer.setModified(true);
        int chance;
        if (talents.isEmpty() && sqlPlayer.getTalentPoints() == 0) {
            chance = total / 2;
        } else {
            chance = sqlPlayer.getTalentChance() - 5;
            chance = Math.max(0, chance);
            chance = Math.min(chance, total / 2);
        }
        int roll = plugin.random.nextInt(total);
        if (roll >= chance) return false;
        addTalentPoints(1);
        return true;
    }

    public void addTalentPoints(final int amount) {
        if (amount == 0) return;
        int points = sqlPlayer.getTalentPoints() + amount;
        sqlPlayer.setTalentPoints(points);
        sqlPlayer.setTalentChance(0);
        sqlPlayer.setModified(true);
        saveData();
        if (amount < 1) return;
        Player player = getPlayer();
        if (player != null) {
            boolean doEffect = true;
            for (SkillType skillType : SkillType.values()) {
                if (plugin.advancements.give(player, skillType)) {
                    doEffect = false;
                }
            }
            int cost = getTalentCost();
            if (points >= cost) {
                if (doEffect) Effects.talentUnlock(player);
                player.showTitle(Title.title(Component.text("TalentType", NamedTextColor.LIGHT_PURPLE),
                                             Component.text("New Unlock Available", NamedTextColor.WHITE)));
            } else {
                if (doEffect) Effects.talentPoint(player);
                player.showTitle(Title.title(Component.text("TalentType Points", NamedTextColor.LIGHT_PURPLE),
                                             Component.text("Progress " + points + "/" + cost, NamedTextColor.WHITE)));
            }
        }
    }

    public boolean unlockTalent(@NonNull TalentType talent) {
        int cost = getTalentCost();
        if (sqlPlayer.getTalentPoints() < cost) return false;
        if (hasTalent(talent)) return false;
        if (!canAccessTalent(talent)) return false;
        sqlPlayer.setTalentPoints(sqlPlayer.getTalentPoints() - cost);
        talents.add(talent);
        sqlPlayer.setTalents(talents.size());
        sqlPlayer.setModified(true);
        tag.setModified(true);
        saveData();
        Player player = getPlayer();
        if (player != null) {
            plugin.advancements.give(player, talent);
        }
        return true;
    }

    public boolean setTalentDisabled(@NonNull TalentType talent, boolean disabled) {
        if (disabledTalents.contains(talent) == disabled) return false;
        if (disabled) {
            disabledTalents.add(talent);
        } else {
            disabledTalents.remove(talent);
        }
        sqlPlayer.setModified(true);
        tag.setModified(true);
        return true;
    }

    private void tick() {
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
                Player player = getPlayer();
                if (player != null) {
                    player.hideBossBar(skillBar);
                }
            }
        }
        if (noSave++ > 200) saveData();
    }

    public void saveData() {
        noSave = 0;
        if (sqlPlayer.getId() != null && (sqlPlayer.isModified() || tag.modified)) {
            if (tag.modified) {
                tag.modified = false;
                tag.talents = talents.stream().map(t -> t.key).collect(Collectors.toSet());
                tag.disabledTalents = disabledTalents.stream().map(t -> t.key).collect(Collectors.toSet());
                sqlPlayer.setJson(Json.serialize(tag));
            }
            sqlPlayer.setModified(false);
            if (plugin.isEnabled()) {
                plugin.database.updateAsync(sqlPlayer, null);
            } else {
                plugin.database.update(sqlPlayer);
            }
        }
        for (SQLSkill row : sqlSkills.values()) {
            if (row.getId() == null || !row.isModified()) continue;
            row.setModified(false);
            if (plugin.isEnabled()) {
                plugin.database.updateAsync(row, null);
            } else {
                plugin.database.update(row);
            }
        }
    }

    public boolean isTalentEnabled(TalentType talent) {
        return talents.contains(talent) && !disabledTalents.contains(talent);
    }

    public boolean hasTalent(@NonNull TalentType talent) {
        return talents.contains(talent);
    }

    public boolean canAccessTalent(@NonNull TalentType talent) {
        return talent.depends == null
            || talents.contains(talent.depends);
    }

    public int getTalentCost() {
        return talents.size() + 1;
    }

    public int getTalentPoints() {
        return sqlPlayer.getTalentPoints();
    }

    public int getLevel(SkillType skillType) {
        return sqlSkills.get(skillType).getLevel();
    }

    public int getSkillPoints(SkillType skillType) {
        return sqlSkills.get(skillType).getPoints();
    }

    public int getExpBonus(SkillType skillType) {
        int level = getLevel(skillType);
        return SkillsPlugin.expBonusForLevel(level);
    }

    /**
     * Load all data but do not prepare for live use!
     */
    public static void loadAsync(UUID uuid, Consumer<Session> callback) {
        Session session = new Session(SkillsPlugin.getInstance(), uuid);
        session.loadAsync(() -> {
                callback.accept(session);
            });
    }

    /**
     * Load all data but do not prepare for live use!
     */
    public static Session loadSync(UUID uuid) {
        Session session = new Session(SkillsPlugin.getInstance(), uuid);
        session.loadSync();
        return session;
    }
}
