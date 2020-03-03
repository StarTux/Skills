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

    public void give(@NonNull Player player,
                     @NonNull SkillType skill,
                     final int add) {
        Session session = plugin.sessions.of(player);
        SQLSkill row = session.skillRows.get(skill);
        int level = row.level;
        int req = forLevel(level + 1);
        double oldProg = (double) row.points / (double) req;
        int points = row.points + add;
        double newProg = (double) points / (double) req;
        boolean levelup = false;
        if (points >= req) {
            levelup = true;
            points -= req;
            req = forLevel(row.level + 2);
            newProg = (double) points / (double) req;
            row.level += 1;
            session.playerRow.levels += 1;
            session.playerRow.modified = true;
            Effects.levelup(player);
            player.sendTitle(ChatColor.GOLD + skill.displayName,
                             ChatColor.WHITE + "Level " + row.level);
        }
        row.points = points;
        row.modified = true;
        session.showSkillBar(player, skill,
                             oldProg, newProg,
                             level, row.level,
                             levelup);
    }
}
