package com.cavetale.skills;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class Points {
    private final SkillsPlugin plugin;

    public static int forLevel(final int lvl) {
        return 100 + lvl * 10 + lvl * lvl;
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
    public void give(@NonNull Player player,
                     @NonNull SkillType skill,
                     final int add) {
        Session session = plugin.sessions.of(player);
        SQLSkill row = session.skillRows.get(skill);
        int oldLevel = row.level;
        int newLevel = oldLevel;
        int req = forLevel(oldLevel + 1);
        int oldPoints = row.points;
        int newPoints = oldPoints + add;
        double oldProg = (double) oldPoints / (double) req;
        double newProg = (double) newPoints / (double) req;
        boolean levelup = false;
        if (newPoints >= req) {
            levelup = true;
            newPoints -= req;
            req = forLevel(oldLevel + 2);
            newProg = (double) newPoints / (double) req;
            newLevel += 1;
            session.playerRow.levels += 1;
            session.playerRow.modified = true;
            Effects.levelup(player);
            player.sendTitle(ChatColor.GOLD + skill.displayName,
                             ChatColor.WHITE + "Level " + newLevel);
        }
        row.points = newPoints;
        row.totalPoints += add;
        row.modified = true;
        row.level = newLevel;
        session.showSkillBar(player, skill,
                             oldProg, newProg,
                             oldLevel, newLevel,
                             levelup);
    }
}
