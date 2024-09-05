package com.cavetale.skills.session;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.sql.SQLTalent;
import com.cavetale.skills.talent.TalentType;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import static com.cavetale.skills.SkillsPlugin.database;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static java.time.Duration.ofSeconds;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.title.Title.Times.times;
import static net.kyori.adventure.title.Title.title;

/**
 * Remember the state of one skill for one player session.
 * Subclasses may exist for individual skill.
 */
public class SkillSession {
    protected final Session session;
    protected final SkillType skillType;
    protected SQLSkill row;

    protected SkillSession(final Session session, final SkillType skillType) {
        this.session = session;
        this.skillType = skillType;
        session.skills.put(skillType, this);
    }

    protected final void load(SQLSkill skillRow) {
        this.row = skillRow;
    }

    protected final void enable() {
        if (row == null) {
            SQLSkill newRow = new SQLSkill(session.uuid, skillType);
            newRow.setRequiredSkillPoints(SkillsPlugin.pointsForLevelUp(1));
            database().insertAsync(newRow, count -> {
                    if (count == 0) {
                        skillsPlugin().getLogger().warning("enable() insert mismatch: " + newRow);
                        onDatabaseMismatch();
                        return;
                    }
                    load(newRow);
                });
        }
    }

    public final boolean isValid() {
        return row != null;
    }

    public final int getSkillPoints() {
        if (row == null) return 0;
        return row.getSkillPoints();
    }

    public final int getRequiredSkillPoints() {
        if (row == null) return 0;
        return row.getRequiredSkillPoints();
    }

    public final void addSkillPoints(final int amount) {
        if (amount <= 0) return;
        if (row == null) return;
        final SQLSkill rowHandle = row;
        database().scheduleAsyncTask(() -> {
                // Do the whole thing in the async thread so that the
                // stored value doesn't go out of sync!
                final int requiredSkillPoints = rowHandle.getRequiredSkillPoints();
                final int newSkillPoints = rowHandle.getSkillPoints() + amount;
                final int newTotalSkillPoints = rowHandle.getTotalSkillPoints() + amount;
                final int result = database().update(SQLSkill.class)
                    .row(rowHandle)
                    .atomic("skillPoints", newSkillPoints)
                    .set("totalSkillPoints", newTotalSkillPoints)
                    .sync();
                Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                        if (result == 0) {
                            skillsPlugin().getLogger().warning("AddSkillPoints mismatch: " + row);
                            onDatabaseMismatch();
                            return;
                        }
                        if (newSkillPoints >= row.getRequiredSkillPoints()) {
                            levelUp();
                        }
                        session.showSkillBar(skillType, getLevel(), newSkillPoints, requiredSkillPoints, amount);
                    });
            });
    }

    public final boolean levelUp() {
        if (row == null) return false;
        if (getSkillPoints() < getRequiredSkillPoints()) return false;
        final SQLSkill rowHandle = row;
        database().scheduleAsyncTask(() -> {
                if (rowHandle.getSkillPoints() < rowHandle.getRequiredSkillPoints()) return;
                final int newLevel = rowHandle.getLevel() + 1;
                final int newSkillPoints = rowHandle.getSkillPoints() - rowHandle.getRequiredSkillPoints();
                final int newTalentPoints = rowHandle.getTalentPoints() + 1;
                final int newTotalTalentPoints = rowHandle.getTotalTalentPoints() + 1;
                final int newRequiredSkillPoints = SkillsPlugin.pointsForLevelUp(newLevel + 1);
                final int result = database().update(SQLSkill.class)
                    .row(rowHandle)
                    .atomic("level", newLevel)
                    .atomic("talentPoints", newTalentPoints)
                    .atomic("totalTalentPoints", newTotalTalentPoints)
                    .atomic("skillPoints", newSkillPoints)
                    .set("requiredSkillPoints", newRequiredSkillPoints)
                    .set("reminder", true)
                    .sync();
                if (result != 0) {
                    database().update(SQLPlayer.class)
                        .row(session.sqlPlayer).add("levels", 1).sync();
                }
                Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                        if (result != 1) {
                            skillsPlugin().getLogger().warning("LevelUp mismatch: " + row);
                            onDatabaseMismatch();
                            return;
                        }
                        Player player = session.getPlayer();
                        if (player != null) {
                            player.showTitle(title(skillType.getIconTitle(),
                                                   text("Level " + newLevel, GOLD),
                                                   times(ofSeconds(1), ofSeconds(1), ofSeconds(1))));
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE,
                                             SoundCategory.MASTER, 0.5f, 2.0f);
                        }
                    });
            });
        return true;
    }

    public final void modifyTalents(final int addPoints, final int addTalents, final Runnable callback) {
        if (row == null) return;
        final SQLSkill rowHandle = row;
        database().scheduleAsyncTask(() -> {
                final int newTalentPoints = rowHandle.getTalentPoints() + addPoints;
                final int newTalents = rowHandle.getTalents() + addTalents;
                final int newTotalTalentPoints = rowHandle.getTotalTalentPoints() + Math.max(0, addPoints);
                final int result = database().update(SQLSkill.class)
                    .row(rowHandle)
                    .atomic("talentPoints", newTalentPoints)
                    .atomic("talents", newTalents)
                    .atomic("totalTalentPoints", newTotalTalentPoints)
                    .sync();
                Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                        if (result != 1) {
                            skillsPlugin().getLogger().warning("ModifyTalents mismatch: " + row);
                            onDatabaseMismatch();
                            return;
                        }
                        if (callback != null) callback.run();
                    });
            });
    }

    /**
     * Call callback with new talent points.
     */
    protected void respec(UUID uuid, Consumer<Integer> callback) {
        database().scheduleAsyncTask(() -> {
                final int addedTalentPoints = row.getTotalTalentPoints() - row.getTalentPoints();
                int res = database().update(SQLSkill.class)
                    .row(row)
                    .atomic("talentPoints", row.getTotalTalentPoints())
                    .atomic("talents", 0)
                    .atomic("moneyBonus", 0)
                    .atomic("expBonus", 0)
                    .sync();
                if (res == 0) {
                    callback.accept(0);
                }
                int deleted = database().find(SQLTalent.class)
                    .eq("player", uuid)
                    .in("talent", TalentType.getTalentKeys(skillType))
                    .delete();
                Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                        callback.accept(addedTalentPoints);
                    });
            });
    }

    public final int getLevel() {
        if (row == null) return 0;
        return row.getLevel();
    }

    public final int getTalentPoints() {
        if (row == null) return 0;
        return row.getTalentPoints();
    }

    public final int getExpBonus() {
        return row != null
            ? row.getExpBonus()
            : 0;
    }

    public final int getMoneyBonus() {
        return row != null
            ? row.getMoneyBonus()
            : 0;
    }

    public final double moneyBonusToPercentage(final int bonus) {
        final int cutoff = 20;
        final int major = Math.min(cutoff, bonus);
        final int minor = Math.max(0, bonus - cutoff);
        return major * 2.5 + minor * 1.0;
    }

    public final double getMoneyBonusPercentage() {
        return moneyBonusToPercentage(getMoneyBonus());
    }

    public final void setSkillLevel(int level) {
        row.setLevel(level);
        database().updateAsync(row, Set.of("level"), null);
    }

    public final boolean isReminder() {
        return row.isReminder();
    }

    public final void setReminder(final boolean value) {
        if (row.isReminder() == value) return;
        row.setReminder(value);
        database().updateAsync(row, Set.of("reminder"), null);
    }

    protected final void increaseMoneyBonus(Runnable callback) {
        database().update(SQLSkill.class)
            .row(row)
            .add("moneyBonus", 1)
            .async(r -> {
                    row.setMoneyBonus(row.getMoneyBonus() + 1);
                    callback.run();
                });
    }

    protected final void increaseExpBonus(Runnable callback) {
        database().update(SQLSkill.class)
            .row(row)
            .add("expBonus", 1)
            .async(r -> {
                    row.setExpBonus(row.getExpBonus() + 1);
                    callback.run();
                });
    }

    /**
     * Whenever the row fails to save, this is called order to trigger
     * a reload.
     * The skill will be invalidated while the loading happens.
     */
    protected void onDatabaseMismatch() {
        if (this.row == null) return; // Already reloading
        this.row = null;
        database().find(SQLSkill.class)
            .eq("player", session.uuid)
            .findUniqueAsync(newRow -> {
                    load(newRow);
                });
    }
}
