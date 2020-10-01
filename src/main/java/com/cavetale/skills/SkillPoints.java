package com.cavetale.skills;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class SkillPoints {
    private final SkillsPlugin plugin;

    public static int forLevel(final int lvl) {
        return 100 + (lvl - 1) * 10;
    }

    public static int totalForLevel(final int lvl) {
        int result = 0;
        for (int i = 1; i <= lvl; i += 1) {
            result += forLevel(i);
        }
        return result;
    }

    /**
     * Add points to the player's account and level them up with all
     * effects if possible.
     */
    public void give(@NonNull Player player, @NonNull SkillType skill, final int add) {
        Session session = plugin.sessions.of(player);
        SQLSkill row = session.skillRows.get(skill);
        int oldLevel = row.level;
        int newLevel = row.level;
        int req = forLevel(newLevel + 1);
        int oldPoints = row.points;
        int newPoints = oldPoints + add;
        boolean levelup = false;
        while (newPoints >= req) {
            session.getSkillBar(skill).skillPointsProgress(newLevel, oldPoints, req, req);
            levelup = true;
            oldPoints = 0;
            newPoints -= req;
            newLevel += 1;
            req = forLevel(newLevel + 1);
            session.playerRow.levels += 1;
            session.playerRow.dirty = true;
            Effects.levelup(player);
            final String title = ChatColor.GOLD + skill.displayName;
            final String subtitle = ChatColor.WHITE + "Level " + newLevel;
            session.getSkillBar(skill).levelUp(newLevel, () -> player.sendTitle(title, subtitle));
        }
        row.points = newPoints;
        row.totalPoints += add;
        row.level = newLevel;
        row.modified = true;
        // session.showSkillBar(player, skill, oldProg, newProg, oldLevel, newLevel, levelup);
        session.getSkillBar(skill).skillPointsProgress(newLevel, oldPoints, newPoints, req);
    }
}
