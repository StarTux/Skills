package com.cavetale.skills;

import com.winthier.generic_events.GenericEvents;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
final class SkillsCommand extends CommandBase implements TabExecutor {
    final SkillsPlugin plugin;

    enum CMD {
        LIST,
        TALENT,
        INFO,
        HI;

        public final String key;

        CMD() {
            key = name().toLowerCase();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player required");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            commandHelp(player);
            return true;
        }
        // SkillType command, e.g. /sk mining
        SkillType skillType;
        try {
            skillType = SkillType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException iae) {
            skillType = null;
        }
        if (skillType != null) {
            if (!skillCommand(player, skillType,
                              Arrays.copyOfRange(args, 1, args.length))) {
                commandHelp(player, skillType);
            }
            return true;
        }
        // Other command
        CMD cmd;
        try {
            cmd = CMD.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException iae) {
            return false;
        }
        try {
            if (!onCommand(player, cmd,
                           Arrays.copyOfRange(args, 1, args.length))) {
                commandHelp(player, cmd);
            }
        } catch (Wrong e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 0) {
            return null;
        }
        String arg = args[args.length - 1];
        if (args.length == 1) {
            return complete(arg, Stream
                            .concat(Stream.of(CMD.values()).map(c -> c.key),
                                    Stream.of(SkillType.values()).map(s -> s.key)));
        }
        CMD cmd;
        try {
            cmd = CMD.valueOf(args[0]);
        } catch (IllegalArgumentException iae) {
            cmd = null;
        }
        SkillType skillType;
        try {
            skillType = SkillType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException iae) {
            skillType = null;
        }
        if (cmd == null && skillType == null) {
            return Collections.emptyList();
        }
        if (cmd != null) {
            return Collections.emptyList();
        }
        if (args.length == 2 && cmd == CMD.INFO) {
            return complete(args[1], plugin.infos.allKeys());
        }
        if (args.length == 2 && cmd == CMD.HI) {
            return complete(arg, Stream
                            .concat(Stream.of("total", "talents"),
                                    Stream.of(SkillType.values())
                                    .map(s -> s.key)));
        }
        return Collections.emptyList();
    }

    boolean onCommand(Player player, CMD cmd, String[] args) throws Wrong {
        switch (cmd) {
        case TALENT:
            return talentCommand(player, args);
        case LIST:
            return listCommand(player, args);
        case INFO:
            return infoCommand(player, args);
        case HI:
            return hiCommand(player, args);
        default:
            return false;
        }
    }

    boolean skillCommand(Player player, SkillType skill, String[] args) {
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

    boolean listCommand(Player player, String[] args) {
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
                           .map(t -> ChatColor.GOLD + t.displayName)
                           .collect(Collectors.joining(ChatColor.DARK_PURPLE + ", ")));
        player.sendMessage("");
        return true;
    }

    boolean infoCommand(Player player, String[] args) throws Wrong {
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

    boolean talentCommand(Player player, String[] args) throws Wrong {
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

    boolean hiCommand(Player player, String[] args) throws Wrong {
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

    void talentMenu(Player player) {
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
            ChatColor talentColor;
            if (session.hasTalent(talent)) {
                cb.append(talent.displayName).color(ChatColor.GREEN);
                talentColor = ChatColor.GREEN;
            } else if (session.canAccessTalent(talent)
                       && session.getTalentPoints() >= session.getTalentCost()) {
                cb.append("[" + talent.displayName + "]").color(ChatColor.GOLD);
                cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/sk talent unlock " + talent.key));
                talentColor = ChatColor.GOLD;
            } else {
                cb.append(talent.displayName).color(ChatColor.GRAY);
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
                    + depColor + talent.depends.displayName;
            }
            cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    TextComponent
                                    .fromLegacyText("" + ChatColor.WHITE + talent.displayName
                                                    + dependency
                                                    + "\n" + talentColor
                                                    + talent.description)));
        }
        player.spigot().sendMessage(cb.create());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Talent Points: "
                           + ChatColor.WHITE + session.getTalentPoints());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Unlock Cost: "
                           + ChatColor.WHITE + session.getTalentCost());
        player.sendMessage("");
    }

    void sendTimeLeft(Player player) {
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

    void commandHelp(final Player player) {
        player.sendMessage(ChatColor.GOLD + "Skills Usage:");
        for (SkillType skillType : SkillType.values()) {
            commandHelp(player, skillType);
        }
        for (CMD cmd : CMD.values()) {
            commandHelp(player, cmd);
        }
        // sendTimeLeft(player);
    }

    void commandHelp(Player player, SkillType skillType) {
        commandHelp(player, skillType.key, null,
                    skillType.displayName + " overview");
    }

    void commandHelp(Player player, CMD cmd) {
        String args = null;
        switch (cmd) {
        case LIST:
            commandHelp(player, cmd.key, null,
                        "List your skills and talents");
            break;
        case TALENT:
            commandHelp(player, cmd.key, null,
                        "Talent overview and management");
            break;
        case INFO:
            commandHelp(player, cmd.key,
                        "[name]",
                        "View info pages");
            break;
        case HI:
            commandHelp(player, cmd.key,
                        "[skill] [page]",
                        "Highscores");
            break;
        default:
            commandHelp(player, cmd.key, null, null);
        }
    }

    private void commandHelp(Player player,
                             String cmd, String args, String desc) {
        String ccmd = ""
            + ChatColor.YELLOW + "/sk "
            + ChatColor.GOLD + cmd
            + (args == null
               ? ""
               : " " + ChatColor.YELLOW + ChatColor.ITALIC + args);
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
}
