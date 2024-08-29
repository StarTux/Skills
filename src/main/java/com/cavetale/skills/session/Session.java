package com.cavetale.skills.session;

import com.cavetale.core.event.hud.PlayerHudEvent;
import com.cavetale.core.event.hud.PlayerHudPriority;
import com.cavetale.mytems.Mytems;
import com.cavetale.skills.TalentMenu;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.sql.SQLTalent;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import static com.cavetale.core.font.Unicode.subscript;
import static com.cavetale.core.font.Unicode.tiny;
import static com.cavetale.skills.SkillsPlugin.database;
import static com.cavetale.skills.SkillsPlugin.moneyBonusPercentage;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

@Getter
public final class Session {
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
    private boolean showSkillBar;
    // Skills
    public final MiningSession mining = new MiningSession(this);
    public final CombatSession combat = new CombatSession(this);
    public final ArcherySession archery = new ArcherySession(this);
    // Status effects, ticks remaining
    @Setter protected boolean netherVisionActive;
    private SkillType talentGui = SkillType.MINING;
    protected final Set<TalentType> debugTalents = EnumSet.noneOf(TalentType.class);
    protected final Set<SkillType> debugSkills = EnumSet.noneOf(SkillType.class);
    protected boolean modifyingTalents = false; // big talent lock

    public Session(@NonNull final UUID uuid) {
        this.uuid = uuid;
    }

    public Session(@NonNull final Player player) {
        this(player.getUniqueId());
    }

    /**
     * Load all data in the current thread.  This should be called
     * from within the async database thread!
     */
    protected void loadPlayer() {
        this.sqlPlayer = database().find(SQLPlayer.class).eq("uuid", uuid).findUnique();
        if (sqlPlayer == null) {
            sqlPlayer = new SQLPlayer(uuid);
            database().insert(sqlPlayer);
        }
        try {
            talentGui = SkillType.values()[sqlPlayer.getTalentGui()];
        } catch (ArrayIndexOutOfBoundsException aioobe) { }
    }

    protected void loadSkills() {
        List<SQLSkill> sqlSkillRows = database().find(SQLSkill.class).eq("player", uuid).findList();
        for (SQLSkill sqlSkill : sqlSkillRows) {
            SkillType skillType = sqlSkill.getSkillType();
            if (skillType == null) {
                skillsPlugin().getLogger().warning("Invalid skill row: " + sqlSkill);
                continue;
            }
            skills.get(skillType).load(sqlSkill);
        }
    }

    protected void loadTalents() {
        List<SQLTalent> sqlTalentRows = database().find(SQLTalent.class).eq("player", uuid).findList();
        for (SQLTalent sqlTalent : sqlTalentRows) {
            TalentType talentType = sqlTalent.getTalentType();
            if (talentType == null) {
                skillsPlugin().getLogger().warning("Invalid talent row: " + sqlTalent);
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
        database().scheduleAsyncTask(() -> {
                loadAll();
                Bukkit.getScheduler().runTask(skillsPlugin(), callback);
            });
    }

    /**
     * Enable for live use.
     */
    protected void enable() {
        if (skillsPlugin().getSessions().getSessionsMap().get(uuid) != this) return;
        enabled = true;
        for (SkillSession skillSession : skills.values()) {
            skillSession.enable();
        }
        skillBar = BossBar.bossBar(text("Skills"),
                                   1.0f,
                                   BossBar.Color.BLUE,
                                   BossBar.Overlay.PROGRESS);
        task = Bukkit.getScheduler().runTaskTimer(skillsPlugin(), this::tick, 1L, 1L);
    }

    protected void disable() {
        enabled = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
        showSkillBar = false;
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

    public void setTalentGui(SkillType skillType) {
        if (talentGui == skillType) return;
        talentGui = skillType;
        sqlPlayer.setTalentGui(skillType.ordinal());
        database().updateAsync(sqlPlayer, Set.of("talentGui"), null);
    }

    protected void showSkillBar(SkillType skillType, int level, int points, int required, int newPoints) {
        if (!enabled) return;
        if (shownSkill == skillType) {
            actionSP += newPoints;
        } else {
            actionSP = newPoints;
        }
        skillBar.name(textOfChildren(skillType.getIconTitle(),
                                     text(tiny(" lvl "), GRAY),
                                     text(level, skillType.textColor, BOLD),
                                     text(tiny(" sp "), GRAY),
                                     text(points, skillType.textColor),
                                     text(subscript("+" + actionSP), skillType.textColor)));
        skillBar.progress(Math.max(0.0f, Math.min(1.0f, (float) points / (float) required)));
        skillBar.color(skillType.bossBarColor);
        shownSkill = skillType;
        skillBarCountdown = 200;
        Player player = getPlayer();
        if (player != null) {
            showSkillBar = true;
        }
    }

    public boolean unlockTalent(@NonNull TalentType talentType, final Runnable callback) {
        if (modifyingTalents) return false;
        if (talents.containsKey(talentType)) return false;
        final int cost = talentType.getTalent().getLevel(1).getTalentPointCost();
        if (getTalentPoints(talentType.skillType) < cost) return false;
        modifyingTalents = true;
        skills.get(talentType.skillType).modifyTalents(-cost, 1, () -> {
                // This is silly because if the talent point removal
                // fails, modifyingTalents will just get stuck until
                // the session is reloaded.
                modifyingTalents = false;
                SQLTalent sqlTalent = new SQLTalent(uuid, talentType, 1);
                talents.put(talentType, sqlTalent);
                database().scheduleAsyncTask(() -> {
                        database().insert(sqlTalent);
                        database().update(SQLPlayer.class)
                            .row(sqlPlayer).add("talents", 1).sync();
                        if (callback != null) {
                            Bukkit.getScheduler().runTask(skillsPlugin(), callback::run);
                        }
                    });
            });
        return true;
    }

    public boolean upgradeTalent(TalentType talentType, Runnable callback) {
        if (modifyingTalents) return false;
        if (!talents.containsKey(talentType)) return false;
        final int currentLevelValue = getTalentLevel(talentType);
        final int nextLevelValue = currentLevelValue + 1;
        if (talentType.getTalent().getMaxLevel().getLevel() < nextLevelValue) return false;
        final int cost = talentType.getTalent().getLevel(nextLevelValue).getTalentPointCost();
        if (getTalentPoints(talentType.skillType) < cost) return false;
        modifyingTalents = true;
        skills.get(talentType.skillType).modifyTalents(-cost, 1, () -> {
                // This is silly because if the talent point removal
                // fails, modifyingTalents will just get stuck until
                // the session is reloaded.
                modifyingTalents = false;
                SQLTalent sqlTalent = talents.get(talentType);
                sqlTalent.setLevel(nextLevelValue);
                database().scheduleAsyncTask(() -> {
                        database().update(sqlTalent, "level");
                        database().update(SQLPlayer.class)
                            .row(sqlPlayer).add("talents", 1).sync();
                        if (callback != null) {
                            Bukkit.getScheduler().runTask(skillsPlugin(), callback::run);
                        }
                    });
            });
        return true;
    }

    public boolean unlockMoneyBonus(SkillType skillType, final Runnable callback) {
        if (modifyingTalents) return false;
        if (getTalentPoints(skillType) < 1) return false;
        modifyingTalents = true;
        skills.get(skillType).modifyTalents(-1, 0, () -> {
                modifyingTalents = false;
                skills.get(skillType).increaseMoneyBonus(callback);
            });
        return true;
    }

    public boolean unlockExpBonus(SkillType skillType, final Runnable callback) {
        if (modifyingTalents) return false;
        if (getTalentPoints(skillType) < 1) return false;
        modifyingTalents = true;
        skills.get(skillType).modifyTalents(-1, 0, () -> {
                modifyingTalents = false;
                skills.get(skillType).increaseExpBonus(callback);
            });
        return true;
    }

    public boolean setTalentEnabled(@NonNull TalentType talentType, boolean value) {
        SQLTalent sqlTalent = talents.get(talentType);
        if (sqlTalent == null || sqlTalent.isEnabled() == value) return false;
        sqlTalent.setEnabled(value);
        database().updateAsync(sqlTalent, null, "enabled");
        return false;
    }

    public boolean isTalentEnabled(TalentType talentType) {
        return talents.containsKey(talentType) && talents.get(talentType).isEnabled();
    }

    public boolean hasTalent(@NonNull TalentType talentType) {
        return talents.containsKey(talentType);
    }

    public int getTalentLevel(TalentType talentType) {
        return talents.containsKey(talentType)
            ? talents.get(talentType).getLevel()
            : 0;
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

    public double computeMoneyDrop(SkillType skillType, final double base) {
        final int bonus = getMoneyBonus(skillType);
        final double factor = 1.0 + 0.01 * moneyBonusPercentage(bonus);
        return base * factor;
    }

    public int getTalentCount(SkillType skillType) {
        return skills.get(skillType).row.getTalents();
    }

    public int getTotalTalentPoints(SkillType skillType) {
        return skills.get(skillType).row.getTotalTalentPoints();
    }

    public int getTalentPointsSpent(SkillType skillType) {
        return getTotalTalentPoints(skillType) - getTalentPoints(skillType);
    }

    public boolean respec(Player player, SkillType skillType) {
        if (modifyingTalents) return false;
        final int talentPointsSpent = getTalentPointsSpent(skillType);
        if (talentPointsSpent == 0) {
            player.sendMessage(textOfChildren(text("You do not have any Talent Points in "),
                                              skillType.getIconComponent()).color(RED));
            return false;
        }
        int spent = 0;
        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack == null || itemStack.getType().isAir()) continue;
            if (Mytems.KITTY_COIN.isItem(itemStack)) {
                itemStack.subtract(1);
                spent += 1;
            }
        }
        if (spent == 0) {
            player.sendMessage(textOfChildren(text("You do not have one "), Mytems.KITTY_COIN, text(" Kitty Coin!")).color(RED));
            return false;
        }
        modifyingTalents = true;
        skills.get(skillType).respec(player.getUniqueId(), talentPointsGiven -> {
                modifyingTalents = false;
                skillsPlugin().getLogger().info(player.getName() + " respec " + skillType + " " + talentPointsGiven + "/" + talentPointsSpent);
                if (talentPointsGiven == 0) {
                    skillsPlugin().getLogger().severe(player.getName() + " respect " + skillType + " failed!");
                    player.sendMessage(text("Something went wrong!", RED));
                } else {
                    player.sendMessage(textOfChildren(text(talentPointsGiven + " "), skillType.getIconTitle(), text(" Talent Points refunded")));
                    talents.keySet().removeAll(TalentType.getTalents(skillType));
                    database().update(SQLPlayer.class)
                        .row(sqlPlayer).set("talents", talents.size()).async(null);
                }
                if (!player.isValid()) return;
                new TalentMenu(player, this).open();
            });
        return true;
    }

    protected void onPlayerHud(PlayerHudEvent event) {
        if (showSkillBar) {
            event.bossbar(PlayerHudPriority.DEFAULT, skillBar.name(),
                          skillBar.color(), skillBar.overlay(), skillBar.progress());
        }
        for (SkillSession sk : skills.values()) {
            if (sk.getTalentPoints() == 0 || !sk.isReminder()) continue;
            var c = sk.skillType.getIconComponent();
            event.sidebar(PlayerHudPriority.LOW,
                          List.of(textOfChildren(c, text("You have more ", AQUA)),
                                  textOfChildren(c, text("/tal", YELLOW), text("ent points", AQUA))));
            break;
        }
    }

    private void tick() {
        if (showSkillBar) {
            skillBarCountdown -= 1;
            if (skillBarCountdown <= 0) {
                shownSkill = null;
                showSkillBar = false;
            }
        }
    }

    /**
     * Load all data but do not prepare for live use!
     */
    public static void loadAsync(UUID uuid, Consumer<Session> callback) {
        Session session = new Session(uuid);
        session.loadAsync(() -> {
                callback.accept(session);
            });
    }

    /**
     * Load all data but do not prepare for live use!
     */
    public static Session loadSync(UUID uuid) {
        Session session = new Session(uuid);
        session.loadAll();
        return session;
    }

    public static Session of(Player player) {
        return skillsPlugin().getSessions().of(player);
    }

    public void setDebugTalent(TalentType talentType, boolean value) {
        if (value) {
            debugTalents.add(talentType);
        } else {
            debugTalents.remove(talentType);
        }
    }

    public boolean toggleDebugTalent(TalentType talentType) {
        if (!debugTalents.contains(talentType)) {
            debugTalents.add(talentType);
            return true;
        } else {
            debugTalents.remove(talentType);
            return false;
        }
    }

    public boolean hasDebugTalent(TalentType talentType) {
        return debugTalents.contains(talentType);
    }

    public void setDebugSkill(SkillType skillType, boolean value) {
        if (value) {
            debugSkills.add(skillType);
        } else {
            debugSkills.remove(skillType);
        }
    }

    public boolean toggleDebugSkill(SkillType skillType) {
        if (!debugSkills.contains(skillType)) {
            debugSkills.add(skillType);
            return true;
        } else {
            debugSkills.remove(skillType);
            return false;
        }
    }

    public boolean hasDebugSkill(SkillType skillType) {
        return debugSkills.contains(skillType);
    }
}
