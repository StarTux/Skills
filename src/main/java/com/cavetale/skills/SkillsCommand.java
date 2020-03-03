package com.cavetale.skills;

import com.winthier.generic_events.GenericEvents;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
final class SkillsCommand implements TabExecutor {
    final SkillsPlugin plugin;
    final List<String> commands = Arrays
        .asList("combat", "mining", "farming", "list", "talent", "info", "hi");

    // Error Class

    static class Wrong extends Exception {
        Wrong(final String msg) {
            super(msg);
        }
    }

    // Overrides

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String alias, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) return false;
            commandHelp((Player) sender);
            return true;
        }
        try {
            String cmd = args[0];
            String[] argl = Arrays.copyOfRange(args, 1, args.length);
            boolean res = onCommand(sender, cmd, argl);
            if (!res) {
                if (commands.contains(cmd)) {
                    commandHelp(requirePlayer(sender), cmd);
                } else {
                    commandHelp(requirePlayer(sender));
                }
            }
            return true;
        } catch (Wrong e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 0) {
            return null;
        }
        String arg = args[args.length - 1];
        if (args.length == 1) {
            return complete(arg, commands);
        }
        if (args.length == 2 && args[0].equals("info")) {
            return complete(args[1], plugin.infos.allKeys());
        }
        if (args.length == 2 && args[0].equals("hi")) {
            return complete(arg, Stream.concat(Stream.of("total", "talents"),
                                               Stream.of(SkillType.values())
                                               .map(s -> s.key)));
        }
        if (args.length == 3 && args[0].equals("hi")) {
            return Collections.emptyList();
        }
        return null;
    }

    // Effective Implementations

    private boolean onCommand(CommandSender sender, String cmd,
                              String[] args) throws Wrong {
        switch (cmd) {
        case "mining":
            return skillCommand(requirePlayer(sender), SkillType.MINING, args);
        case "farming":
            return skillCommand(requirePlayer(sender), SkillType.FARMING, args);
        case "combat":
            return skillCommand(requirePlayer(sender), SkillType.COMBAT, args);
        case "talent":
            return talentCommand(requirePlayer(sender), args);
        case "list":
            return listCommand(requirePlayer(sender), args);
        case "info":
            return infoCommand(requirePlayer(sender), args);
        case "hi":
            return hiCommand(requirePlayer(sender), args);
        case "reloadadvancements": {
            if (!sender.isOp()) return false;
            sender.sendMessage("Reloading advancements...");
            plugin.advancements.unloadAll();
            plugin.advancements.loadAll();
            sender.sendMessage("Advancements reloaded.");
            return true;
        }
        case "gimme": {
            Player player = requirePlayer(sender);
            if (!player.isOp()) return false;
            plugin.talents.addPoints(player, 1);
            return true;
        }
        case "particles": {
            Player player = requirePlayer(sender);
            if (!player.isOp()) return false;
            Session session = plugin.sessions.of(player);
            session.noParticles = !session.noParticles;
            player.sendMessage("Particles: " + (session.noParticles ? "off" : "on"));
            return true;
        }
        case "median": {
            if (!sender.isOp()) return false;
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
        default:
            return false;
        }
    }

    boolean skillCommand(@NonNull Player player, @NonNull SkillType skill,
                         String[] args) {
        if (args.length != 0) return false;
        Session session = plugin.sessions.of(player);
        int level = session.getLevel(skill);
        int points = session.getSkillPoints(skill);
        int req = Points.forLevel(level + 1);
        long talents = Stream.of(Talent.values())
            .filter(t -> t.skill == skill).count();
        long talentsHas = Stream.of(Talent.values())
            .filter(t -> t.skill == skill)
            .filter(session::hasTalent).count();
        player.sendMessage("");
        player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + skill.displayName);
        player.sendMessage("");
        Info info = plugin.infos.get(skill.key);
        if (info != null) {
            player.sendMessage(info.description.split("\n\n")[0]);
        }
        player.sendMessage("");
        player.sendMessage("" + ChatColor.LIGHT_PURPLE + "Level "
                           + ChatColor.WHITE + level);
        player.sendMessage("" + ChatColor.LIGHT_PURPLE + "Skill Points "
                           + ChatColor.WHITE + points
                           + ChatColor.LIGHT_PURPLE + "/"
                           + ChatColor.WHITE + req);
        player.sendMessage("" + ChatColor.LIGHT_PURPLE + "Talents "
                           + ChatColor.WHITE + talentsHas
                           + ChatColor.LIGHT_PURPLE + "/"
                           + ChatColor.WHITE + talents);
        player.sendMessage("");
        return true;
    }

    boolean listCommand(@NonNull Player player, String[] args) {
        if (args.length != 0) return false;
        player.sendMessage("");
        Session session = plugin.sessions.of(player);
        for (SkillType skill : SkillType.values()) {
            int level = session.getLevel(skill);
            int points = session.getSkillPoints(skill);
            int req = plugin.points.forLevel(level + 1);
            player.sendMessage(""
                               + ChatColor.DARK_PURPLE + "lvl"
                               + ChatColor.YELLOW + ChatColor.BOLD + level
                               + ChatColor.LIGHT_PURPLE + " " + skill.displayName
                               + ChatColor.WHITE + " " + points
                               + ChatColor.LIGHT_PURPLE + "/"
                               + ChatColor.WHITE + req);
        }
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Talents: "
                           + Stream.of(Talent.values())
                           .filter(session::hasTalent)
                           .map(t -> ChatColor.GOLD + plugin.talents.getInfo(t).title)
                           .collect(Collectors.joining(ChatColor.DARK_PURPLE + ", ")));
        player.sendMessage("");
        return true;
    }

    boolean infoCommand(@NonNull Player player, String[] args) throws Wrong {
        if (args.length > 1) return false;
        if (args.length == 1) {
            Info info = plugin.infos.get(args[0]);
            if (info == null) {
                throw new Wrong("Not found: " + args[0]);
            }
            player.sendMessage("");
            player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + info.title);
            for (String p : info.description.split("\n\n")) {
                player.sendMessage("");
                player.sendMessage(p);
            }
            player.sendMessage("");
            return true;
        }
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Pages: "
                           + plugin.infos.allKeys().stream()
                           .map(s -> ChatColor.YELLOW + s)
                           .collect(Collectors.joining(ChatColor.DARK_PURPLE + ", ")));
        return true;
    }

    boolean talentCommand(@NonNull Player player, String[] args) throws Wrong {
        if (args.length == 0) {
            talentMenu(player);
            return true;
        }
        switch (args[0]) {
        case "unlock": {
            if (args.length != 2) return false;
            Session session = plugin.sessions.of(player);
            if (session.getTalentPoints() < session.getTalentCost()) {
                throw new Wrong("You don't have enough Talent Points!");
            }
            Talent talent = Talent.of(args[1]);
            if (talent == null) {
                throw new Wrong("Invalid talent!");
            }
            if (session.hasTalent(talent)) {
                throw new Wrong("Already unlocked!");
            }
            if (!session.canAccessTalent(talent)) {
                throw new Wrong("Parent not yet available!");
            }
            if (!plugin.talents.unlock(player, talent)) {
                throw new Wrong("An unknown error occured.");
            }
            Effects.talentUnlock(player);
            talentMenu(player);
            return true;
        }
        default: break;
        }
        return false;
    }

    @Value
    static final class Score implements Comparable<Score> {
        final int score;
        final UUID uuid;

        @Override
        public int compareTo(Score o) {
            return Integer.compare(o.score, score);
        }
    }

    boolean hiCommand(@NonNull Player player, String[] args) throws Wrong {
        if (args.length == 0) {
            ComponentBuilder cb = new ComponentBuilder("Options: ")
                .color(ChatColor.LIGHT_PURPLE);
            String cmd = "/sk hi total";
            cb.append("Total").color(ChatColor.YELLOW);
            cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
            cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    TextComponent
                                    .fromLegacyText(ChatColor.YELLOW + cmd)));
            cb.append(", ").color(ChatColor.DARK_PURPLE);
            cmd = "/sk hi talents";
            cb.append("Talents").color(ChatColor.YELLOW);
            cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
            cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    TextComponent
                                    .fromLegacyText(ChatColor.YELLOW + cmd)));
            for (SkillType skill : SkillType.values()) {
                cb.append(", ").color(ChatColor.DARK_PURPLE);
                cmd = "/sk hi " + skill.key;
                cb.append(skill.displayName).color(ChatColor.GOLD);
                cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
                cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        TextComponent
                                        .fromLegacyText(ChatColor.GOLD + cmd)));
            }
            player.spigot().sendMessage(cb.create());
            return true;
        }
        if (args.length > 2) return false;
        // Skill
        SkillType skill = SkillType.ofKey(args[0]);
        if (!"total".equals(args[0]) && !"talents".equals(args[0]) && skill == null) {
            throw new Wrong("Invalid skill: " + args[0]);
        }
        // Page Number
        int page = 0;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]) - 1;
            } catch (NumberFormatException nfe) {
                page = -1;
            }
            if (page < 0) {
                throw new Wrong("Invalid page number: " + args[1]);
            }
        }
        // Collect
        List<Score> scores;
        final String title;
        if (skill == null) {
            if (args[0].equals("total")) {
                title = "Total";
                scores = plugin.sql.playerRows.values().stream()
                    .filter(p -> p.levels > 0)
                    .map(p -> new Score(p.levels, p.uuid))
                    .collect(Collectors.toList());
            } else if (args[0].equals("talents")) {
                title = "Talents";
                scores = plugin.sql.playerRows.values().stream()
                    .filter(p -> p.talents > 0)
                    .map(p -> new Score(p.talents, p.uuid))
                    .collect(Collectors.toList());
            } else {
                throw new IllegalStateException("arg=" + args[0]);
            }
        } else {
            title = skill.displayName;
            scores = plugin.sql.skillRows.stream()
                .filter(s -> s.level > 0)
                .filter(s -> skill.key.equals(s.skill))
                .map(s -> new Score(s.level, s.player))
                .collect(Collectors.toList());
        }
        int offset = page * 10;
        if (offset >= scores.size()) {
            throw new Wrong("Page " + (page + 1) + " unavailable!");
        }
        Collections.sort(scores);
        player.sendMessage("");
        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD
                           + title + " Highscore");
        for (int i = 0; i < 10; i += 1) {
            final int index = offset + i;
            final int rank = index + 1;
            final String level;
            final String name;
            if (index < scores.size()) {
                Score score = scores.get(index);
                level = "" + score.score;
                name = GenericEvents.cachedPlayerName(score.uuid);
            } else {
                level = "?";
                name = " ---";
            }
            player.sendMessage(""
                               + ChatColor.DARK_PURPLE + "#" + rank
                               + ChatColor.WHITE + " " + level
                               + ChatColor.LIGHT_PURPLE + " " + name);
        }
        return true;
    }

    void talentMenu(@NonNull Player player) {
        player.sendMessage("");
        player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "Skill Talents");
        Session session = plugin.sessions.of(player);
        ComponentBuilder cb = null;
        for (Talent talent : Talent.values()) {
            if (cb != null && talent.depends == null) {
                player.spigot().sendMessage(cb.create());
                cb = null;
            }
            if (cb == null) {
                cb = new ComponentBuilder("");
            }
            cb.append("  ").reset();
            TalentInfo info = plugin.talents.getInfo(talent);
            ChatColor talentColor;
            if (session.hasTalent(talent)) {
                cb.append(info.title).color(ChatColor.GREEN);
                talentColor = ChatColor.GREEN;
            } else if (session.canAccessTalent(talent)
                       && session.getTalentPoints() >= session.getTalentCost()) {
                cb.append("[" + info.title + "]").color(ChatColor.GOLD);
                cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/sk talent unlock " + talent.key));
                talentColor = ChatColor.GOLD;
            } else {
                cb.append(info.title).color(ChatColor.GRAY);
                talentColor = ChatColor.GRAY;
            }
            String dependency;
            if (talent.depends == null) {
                dependency = "";
            } else {
                ChatColor depColor = session.hasTalent(talent.depends)
                    ? ChatColor.GREEN
                    : ChatColor.DARK_RED;
                dependency = ChatColor.LIGHT_PURPLE + "\nRequires: "
                    + depColor + plugin.talents.getInfo(talent.depends).title;
            }
            cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    TextComponent
                                    .fromLegacyText("" + ChatColor.WHITE + info.title
                                                    + dependency
                                                    + "\n" + talentColor
                                                    + info.description)));
        }
        player.spigot().sendMessage(cb.create());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Talent Points: "
                           + ChatColor.WHITE + session.getTalentPoints());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Unlock Cost: "
                           + ChatColor.WHITE + session.getTalentCost());
        player.sendMessage("");
    }

    void sendTimeLeft(@NonNull Player player) {
        long dst = 1573063200000L - System.currentTimeMillis();
        long seconds = dst / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        String fmt = String.format("%d days %02d:%02d",
                                   days, hours % 24, minutes % 60);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Time Left: "
                           + ChatColor.WHITE + fmt);
    }

    void commandHelp(@NonNull final Player player) {
        player.sendMessage(ChatColor.GOLD + "Skills Usage:");
        for (String cmd : commands) {
            commandHelp(player, cmd);
        }
        // sendTimeLeft(player);
    }

    void commandHelp(@NonNull Player player, @NonNull String cmd) {
        final String desc;
        String args = null;
        switch (cmd) {
        case "combat": case "mining": case "farming":
            desc = "Skill overview";
            break;
        case "list":
            desc = "List your skills and talents";
            break;
        case "talent":
            desc = "Talent overview and management";
            break;
        case "info":
            desc = "View info pages";
            args = " [name]";
            break;
        case "hi":
            desc = "Highscores";
            args = " [skill] [page]";
            break;
        default:
            desc = null;
            break;
        }
        String ccmd = ""
            + ChatColor.YELLOW + "/sk "
            + ChatColor.GOLD + cmd
            + (args == null ? ""
               : "" + ChatColor.YELLOW + ChatColor.ITALIC + args);
        String cdesc = desc == null
            ? ""
            : ChatColor.DARK_GRAY + " - " + ChatColor.WHITE + desc;
        String tooltip = ccmd + ChatColor.RESET + "\n" + desc;
        ComponentBuilder cb = new ComponentBuilder(ccmd + cdesc);
        cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sk " +  cmd));
        cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                TextComponent.fromLegacyText(tooltip)));
        player.spigot().sendMessage(cb.create());
    }

    // Helpers

    private List<String> complete(final String arg,
                                  final Collection<String> opt) {
        return opt.stream().filter(o -> o.startsWith(arg))
            .collect(Collectors.toList());
    }

    private List<String> complete(final String arg,
                                  final Stream<String> opt) {
        return opt.filter(o -> o.startsWith(arg))
            .collect(Collectors.toList());
    }

    // Wrong Throwers

    Player requirePlayer(final CommandSender sender) throws Wrong {
        if (!(sender instanceof Player)) {
            throw new Wrong("Player required");
        }
        return (Player) sender;
    }
}
