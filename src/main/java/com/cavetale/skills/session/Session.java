package com.cavetale.skills.session;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.sql.SQLTalent;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@Getter
public final class Session {
    protected final SkillsPlugin plugin;
    protected final UUID uuid;
    protected boolean enabled = false;
    protected SQLPlayer sqlPlayer;
    protected final EnumMap<SkillType, SkillSession> skills = new EnumMap<>(SkillType.class);
    protected final EnumMap<TalentType, SQLTalent> talents = new EnumMap<>(TalentType.class);
    protected BukkitTask task;
    // Skill Bar
    protected BossBar skillBar;
    protected SkillType shownSkill = null;
    protected int skillBarCountdown;
    private int actionSP;
    // Skills
    public final SkillSession mining = new SkillSession(this, SkillType.MINING);
    public final CombatSession combat = new CombatSession(this, SkillType.COMBAT);
    // Status effects, ticks remaining
    @Setter protected boolean superVisionActive;
    @Setter protected boolean netherVisionActive;
    @Setter protected int archerZone = 0;
    @Setter protected int archerZoneKills = 0;
    @Setter protected boolean noParticles = false;
    @Setter protected SkillType talentGui = SkillType.MINING;
    protected boolean unlockingTalent = false; // big talent lock

    public Session(@NonNull final SkillsPlugin plugin, @NonNull final UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
    }

    public Session(@NonNull final SkillsPlugin plugin, @NonNull final Player player) {
        this(plugin, player.getUniqueId());
    }

    /**
     * Load all data in the current thread.  This should be called
     * from within the async database thread!
     */
    protected void loadPlayer() {
        this.sqlPlayer = plugin.database.find(SQLPlayer.class).eq("uuid", uuid).findUnique();
        if (sqlPlayer == null) {
            sqlPlayer = new SQLPlayer(uuid);
            plugin.database.insert(sqlPlayer);
        }
    }

    protected void loadSkills() {
        List<SQLSkill> sqlSkillRows = plugin.database.find(SQLSkill.class).eq("player", uuid).findList();
        for (SQLSkill sqlSkill : sqlSkillRows) {
            SkillType skillType = sqlSkill.getSkillType();
            if (skillType == null) {
                plugin.getLogger().warning("Invalid skill row: " + sqlSkill);
                continue;
            }
            skills.get(skillType).load(sqlSkill);
        }
    }

    protected void loadTalents() {
        List<SQLTalent> sqlTalentRows = plugin.database.find(SQLTalent.class).eq("player", uuid).findList();
        for (SQLTalent sqlTalent : sqlTalentRows) {
            TalentType talentType = sqlTalent.getTalentType();
            if (talentType == null) {
                plugin.getLogger().warning("Invalid talent row: " + sqlTalent);
                continue;
            }
            talents.put(talentType, sqlTalent);
        }
    }

    protected void loadAll() {
        loadPlayer();
        loadSkills();
        loadTalents();
    }

    protected void loadAsync(final Runnable callback) {
        plugin.database.scheduleAsyncTask(() -> {
                loadAll();
                Bukkit.getScheduler().runTask(plugin, callback);
            });
    }

    /**
     * Enable for live use.
     */
    protected void enable() {
        if (plugin.sessions.sessionsMap.get(uuid) != this) return;
        enabled = true;
        for (SkillSession skillSession : skills.values()) {
            skillSession.enable();
        }
        skillBar = BossBar.bossBar(Component.text("Skills"),
                                   1.0f,
                                   BossBar.Color.BLUE,
                                   BossBar.Overlay.PROGRESS);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
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

    public SkillSession getSkill(SkillType skillType) {
        return skills.get(skillType);
    }

    public void addSkillPoints(SkillType skillType, int amount) {
        skills.get(skillType).addSkillPoints(amount);
    }

    public int getRequiredSkillPoints(SkillType skillType) {
        return skills.get(skillType).getRequiredSkillPoints();
    }

    public int getSkillPoints(SkillType skillType) {
        return skills.get(skillType).getSkillPoints();
    }

    public int getLevel(SkillType skillType) {
        return skills.get(skillType).getLevel();
    }

    public boolean levelUp(SkillType skillType) {
        return skills.get(skillType).levelUp();
    }

    public int getTalentPoints(SkillType skillType) {
        return skills.get(skillType).getTalentPoints();
    }

    protected void showSkillBar(SkillType skillType, int level, int points, int required, int newPoints) {
        if (shownSkill == skillType) {
            actionSP += newPoints;
        } else {
            actionSP = newPoints;
        }
        skillBar.name(Component.join(JoinConfiguration.noSeparators(),
                                     Component.text(skillType.displayName, skillType.tag.color()),
                                     Component.text(" Level "),
                                     Component.text(level + " ", skillType.tag.color(), TextDecoration.BOLD),
                                     Component.text(points),
                                     Component.text("/", NamedTextColor.DARK_GRAY),
                                     Component.text(required))
                      .color(NamedTextColor.GRAY));
        skillBar.progress((float) points / (float) required);
        skillBar.color(skillType.tag.bossBarColor());
        shownSkill = skillType;
        skillBarCountdown = 100;
        Player player = getPlayer();
        if (player != null) {
            player.showBossBar(skillBar);
            player.sendActionBar(Component.join(JoinConfiguration.noSeparators(),
                                                Component.text("+"),
                                                Component.text(actionSP, skillType.tag.color(), TextDecoration.BOLD),
                                                Component.text("SP"))
                                 .color(NamedTextColor.GRAY));
        }
    }

    public boolean unlockTalent(@NonNull TalentType talentType, final Runnable callback) {
        if (unlockingTalent) return false;
        if (talents.containsKey(talentType)) return false;
        final int cost = talentType.talentPointCost;
        if (getTalentPoints(talentType.skillType) < cost) return false;
        unlockingTalent = true;
        skills.get(talentType.skillType).modifyTalents(-cost, 1, () -> {
                // This is silly because if the talent point removal
                // fails, unlockingTalent will just get stuck until
                // the session is reloaded.
                unlockingTalent = false;
                SQLTalent sqlTalent = new SQLTalent(uuid, talentType);
                talents.put(talentType, sqlTalent);
                plugin.database.insertAsync(sqlTalent, null);
                sqlPlayer.setTalents(sqlPlayer.getTalents() + 1);
                plugin.database.updateAsync(sqlPlayer, null, "talents");
                if (callback != null) callback.run();
            });
        return true;
    }

    public boolean unlockMoneyBonus(SkillType skillType, final Runnable callback) {
        if (unlockingTalent) return false;
        if (getTalentPoints(skillType) < 1) return false;
        unlockingTalent = true;
        skills.get(skillType).modifyTalents(-1, 0, () -> {
                unlockingTalent = false;
                skills.get(skillType).increaseMoneyBonus(callback);
            });
        return true;
    }

    public boolean unlockExpBonus(SkillType skillType, final Runnable callback) {
        if (unlockingTalent) return false;
        if (getTalentPoints(skillType) < 1) return false;
        unlockingTalent = true;
        skills.get(skillType).modifyTalents(-1, 0, () -> {
                unlockingTalent = false;
                skills.get(skillType).increaseExpBonus(callback);
            });
        return true;
    }

    public boolean setTalentEnabled(@NonNull TalentType talentType, boolean value) {
        SQLTalent sqlTalent = talents.get(talentType);
        if (sqlTalent == null || sqlTalent.isEnabled() == value) return false;
        sqlTalent.setEnabled(value);
        plugin.database.updateAsync(sqlTalent, null, "enabled");
        return false;
    }

    public boolean isTalentEnabled(TalentType talentType) {
        return talents.containsKey(talentType) && talents.get(talentType).isEnabled();
    }

    public boolean hasTalent(@NonNull TalentType talentType) {
        return talents.containsKey(talentType);
    }

    public boolean canAccessTalent(@NonNull TalentType talentType) {
        return talentType.depends == null
            || talents.containsKey(talentType.depends);
    }

    public int getExpBonus(SkillType skillType) {
        return skills.get(skillType).getExpBonus();
    }

    public int getMoneyBonus(SkillType skillType) {
        return skills.get(skillType).getMoneyBonus();
    }

    private void tick() {
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
        session.loadAll();
        return session;
    }
}
