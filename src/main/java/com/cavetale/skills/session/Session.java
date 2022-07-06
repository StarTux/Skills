package com.cavetale.skills.session;

import com.cavetale.core.event.hud.PlayerHudEvent;
import com.cavetale.core.event.hud.PlayerHudPriority;
import com.cavetale.mytems.Mytems;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.sql.SQLTalent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import static com.cavetale.core.font.Unicode.tiny;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

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
    private boolean showSkillBar;
    // Skills
    public final SkillSession mining = new SkillSession(this, SkillType.MINING);
    public final CombatSession combat = new CombatSession(this, SkillType.COMBAT);
    // Status effects, ticks remaining
    @Setter protected boolean superVisionActive;
    @Setter protected boolean netherVisionActive;
    @Setter protected int archerZone = 0;
    @Setter protected int archerZoneKills = 0;
    @Setter protected SkillType talentGui = SkillType.MINING;
    @Setter protected boolean debugMode;
    protected boolean modifyingTalents = false; // big talent lock

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
        skillBar = BossBar.bossBar(text("Skills"),
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

    protected void showSkillBar(SkillType skillType, int level, int points, int required, int newPoints) {
        if (shownSkill == skillType) {
            actionSP += newPoints;
        } else {
            actionSP = newPoints;
        }
        skillBar.name(join(noSeparators(),
                           skillType,
                           text(tiny(" lvl "), GRAY),
                           text(level, skillType.tag.color(), BOLD),
                           text(tiny(" sp "), GRAY),
                           text(points, skillType.tag.color())));
        skillBar.progress(Math.max(0.0f, Math.min(1.0f, (float) points / (float) required)));
        skillBar.color(skillType.tag.bossBarColor());
        shownSkill = skillType;
        skillBarCountdown = 100;
        Player player = getPlayer();
        if (player != null) {
            showSkillBar = true;
            player.sendActionBar(join(noSeparators(),
                                      text("+"),
                                      text(actionSP, skillType.tag.color(), BOLD),
                                      text("SP"))
                                 .color(GRAY));
        }
    }

    public boolean unlockTalent(@NonNull TalentType talentType, final Runnable callback) {
        if (modifyingTalents) return false;
        if (talents.containsKey(talentType)) return false;
        final int cost = talentType.talentPointCost;
        if (getTalentPoints(talentType.skillType) < cost) return false;
        modifyingTalents = true;
        skills.get(talentType.skillType).modifyTalents(-cost, 1, () -> {
                // This is silly because if the talent point removal
                // fails, modifyingTalents will just get stuck until
                // the session is reloaded.
                modifyingTalents = false;
                SQLTalent sqlTalent = new SQLTalent(uuid, talentType);
                talents.put(talentType, sqlTalent);
                plugin.database.insertAsync(sqlTalent, null);
                plugin.database.update(SQLPlayer.class)
                    .row(sqlPlayer).add("talents", 1).async(null);
                if (callback != null) callback.run();
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

    public int getTotalSpentTalentPoints(SkillType skillType) {
        int result = 0;
        for (TalentType it : talents.keySet()) {
            if (it.skillType != skillType) continue;
            result += it.talentPointCost;
        }
        result += getExpBonus(skillType);
        result += getMoneyBonus(skillType);
        return result;
    }

    public boolean respec(Player player, SkillType skillType) {
        if (modifyingTalents) return false;
        int talentPoints = 0;
        List<SQLTalent> talentRows = new ArrayList<>();
        for (Iterator<Map.Entry<TalentType, SQLTalent>> iter = talents.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<TalentType, SQLTalent> entry = iter.next();
            TalentType talentType = entry.getKey();
            if (talentType.skillType == skillType) {
                talentPoints += talentType.talentPointCost;
                talentRows.add(entry.getValue());
                iter.remove();
            }
        }
        talentPoints += getExpBonus(skillType);
        talentPoints += getMoneyBonus(skillType);
        if (talentPoints == 0) {
            player.sendMessage(join(noSeparators(), text("You do not have any Talent Points in "), skillType).color(RED));
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
            player.sendMessage(join(noSeparators(), text("You do not have one "), Mytems.KITTY_COIN, text(" Kitty Coin!")).color(RED));
            return false;
        }
        modifyingTalents = true;
        final int talentPoints2 = talentPoints;
        skills.get(skillType).respec(player, talentRows, talentPoints, tp -> {
                modifyingTalents = false;
                plugin.getLogger().info(player.getName() + " respec " + skillType + " " + tp + "/" + talentPoints2);
                if (tp == 0) {
                    plugin.getLogger().severe(player.getName() + " respect " + skillType + " failed!");
                    player.sendMessage(text("Something went wrong!", RED));
                } else {
                    player.sendMessage(join(noSeparators(), text(tp + " "), skillType, text(" Talent Points refunded")));
                }
                if (!player.isValid()) return;
                plugin.guis.talents(player);
            });
        return true;
    }

    protected void onPlayerHud(PlayerHudEvent event) {
        if (showSkillBar || debugMode) {
            event.bossbar(PlayerHudPriority.DEFAULT,
                          (debugMode
                           ? join(noSeparators(), text("Debug ", YELLOW, BOLD), skillBar.name())
                           : skillBar.name()),
                          skillBar.color(), skillBar.overlay(), skillBar.progress());
        }
    }

    private void tick() {
        if (archerZone > 0) {
            archerZone -= 1;
            if (archerZone == 0) archerZoneKills = 0;
        }
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
