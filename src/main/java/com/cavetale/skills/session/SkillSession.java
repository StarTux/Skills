package com.cavetale.skills.session;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.sql.SQLSkill;
import org.bukkit.Bukkit;

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
            session.plugin.database.insertAsync(newRow, count -> {
                    if (count == 0) {
                        session.plugin.getLogger().warning("enable() insert mismatch: " + newRow);
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
        if (row == null) return;
        if (getSkillPoints() >= getRequiredSkillPoints()) return;
        final int totalAmount = Math.min(row.getRequiredSkillPoints() - row.getSkillPoints(), amount);
        final int newSkillPoints = row.getSkillPoints() + totalAmount;
        final int newTotalSkillPoints = row.getTotalSkillPoints() + totalAmount;
        final SQLSkill rowHandle = row;
        session.plugin.database.scheduleAsyncTask(() -> {
                // Do the whole thing in the async thread so that the
                // stored value doesn't go out of sync!
                final int result = session.plugin.database.update(SQLSkill.class)
                    .row(rowHandle)
                    .atomic("skill_points", newSkillPoints)
                    .set("total_skill_points", newTotalSkillPoints)
                    .sync();
                Bukkit.getScheduler().runTask(session.plugin, () -> {
                        if (result != 1) {
                            session.plugin.getLogger().warning("AddSkillPoints mismatch: " + row);
                            onDatabaseMismatch();
                            return;
                        }
                        session.showSkillBar(skillType, getLevel(), getSkillPoints(), getRequiredSkillPoints(), amount);
                    });
            });
    }

    public final boolean levelUp() {
        if (row == null) return false;
        if (getSkillPoints() < getRequiredSkillPoints()) return false;
        int newLevel = row.getLevel() + 1;
        int newTalentPoints = row.getTalentPoints() + 1;
        int newRequiredSkillPoints = SkillsPlugin.pointsForLevelUp(newLevel + 1);
        final SQLSkill rowHandle = row;
        session.plugin.database.scheduleAsyncTask(() -> {
                final int result = session.plugin.database.update(SQLSkill.class)
                    .row(rowHandle)
                    .atomic("level", newLevel)
                    .atomic("talent_points", newTalentPoints)
                    .set("skill_points", 0)
                    .set("required_skill_points", newRequiredSkillPoints)
                    .sync();
                Bukkit.getScheduler().runTask(session.plugin, () -> {
                        if (result != 1) {
                            session.plugin.getLogger().warning("LevelUp mismatch: " + row);
                            onDatabaseMismatch();
                            return;
                        }
                        session.showSkillBar(skillType, newLevel, 0, newRequiredSkillPoints, 0);
                        if (newLevel == 1) {
                            var player = session.getPlayer();
                            if (player != null) {
                                session.plugin.advancements.give(player, skillType);
                            }
                        }
                    });
            });
        return true;
    }

    public final void modifyTalents(final int addPoints, final int addTalents, final Runnable callback) {
        if (row == null) return;
        final int newTalentPoints = row.getTalentPoints() + addPoints;
        final int newTalents = row.getTalents() + addTalents;
        final SQLSkill rowHandle = row;
        session.plugin.database.scheduleAsyncTask(() -> {
                final int result = session.plugin.database.update(SQLSkill.class)
                    .row(rowHandle)
                    .atomic("talent_points", newTalentPoints)
                    .atomic("talents", newTalents)
                    .sync();
                Bukkit.getScheduler().runTask(session.plugin, () -> {
                        if (result != 1) {
                            session.plugin.getLogger().warning("LevelUp mismatch: " + row);
                            onDatabaseMismatch();
                            return;
                        }
                        if (callback != null) callback.run();
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
        if (row == null) return 0;
        int level = getLevel();
        return SkillsPlugin.expBonusForLevel(level);
    }

    /**
     * Whenever the row fails to save, this is called order to trigger
     * a reload.
     * The skill will be invalidated while the loading happens.
     */
    protected void onDatabaseMismatch() {
        if (this.row == null) return; // Already reloading
        this.row = null;
        session.plugin.database.find(SQLSkill.class)
            .eq("player", session.uuid)
            .findUniqueAsync(newRow -> {
                    load(newRow);
                });
    }
}
