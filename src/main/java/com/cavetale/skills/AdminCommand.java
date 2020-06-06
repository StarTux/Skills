package com.cavetale.skills;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class AdminCommand extends CommandBase implements TabExecutor {
    private final SkillsPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String alias, String[] args) {
        if (args.length == 0) return false;
        try {
            return onCommand(sender, args[0],
                             Arrays.copyOfRange(args, 1, args.length));
        } catch (Wrong wrong) {
            sender.sendMessage(wrong.getMessage());
            return true;
        }
    }

    boolean onCommand(CommandSender sender, String cmd, String[] args) throws Wrong {
        switch (cmd) {
        case "reloadadvancements": return reloadAdvancementsCommand(sender, args);
        case "gimme": return gimmeCommand(sender, args);
        case "particles": return particlesCommand(sender, args);
        case "median": return medianCommand(sender, args);
        default: return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        return null;
    }

    boolean reloadAdvancementsCommand(CommandSender sender, String[] args) throws Wrong {
        if (args.length != 0) return false;
        sender.sendMessage("Reloading advancements...");
        plugin.advancements.unloadAll();
        plugin.advancements.loadAll();
        sender.sendMessage("Advancements reloaded.");
        return true;
    }

    boolean gimmeCommand(CommandSender sender, String[] args) throws Wrong {
        if (args.length != 0) return false;
        Player player = requirePlayer(sender);
        plugin.talents.addPoints(player, 1);
        return true;
    }

    boolean particlesCommand(CommandSender sender, String[] args) throws Wrong {
        if (args.length != 0) return false;
        Player player = requirePlayer(sender);
        Session session = plugin.sessions.of(player);
        session.noParticles = !session.noParticles;
        player.sendMessage("Particles: " + (session.noParticles ? "off" : "on"));
        return true;
    }

    boolean medianCommand(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        for (SkillType skill : SkillType.values()) {
            List<SQLSkill> rows = plugin.sql.skillRows.stream()
                .filter(s -> s.level > 0)
                .filter(s -> skill.key.equals(s.skill))
                .sorted((b, a) -> Integer.compare(a.totalPoints,
                                                  b.totalPoints))
                .collect(Collectors.toList());
            if (rows.isEmpty()) continue;
            int sumSP = 0;
            int sumLevel = 0;
            for (SQLSkill row : rows) {
                sumSP += row.totalPoints;
                sumLevel += row.level;
            }
            int avgSP = sumSP / rows.size();
            int avgLevel = sumLevel / rows.size();
            SQLSkill median = rows.get(rows.size() / 2);
            SQLSkill max = rows.get(0);
            sender.sendMessage(skill.displayName
                               + "\t"
                               + " Sample=" + rows.size()
                               + " Sum=" + sumSP + "," + sumLevel
                               + " Avg=" + avgSP + "," + avgLevel
                               + " Max=" + max.totalPoints + "," + max.level
                               + " Med=" + median.totalPoints + "," + median.level);
        }
        return true;
    }
}
