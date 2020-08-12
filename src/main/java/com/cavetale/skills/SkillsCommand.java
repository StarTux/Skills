package com.cavetale.skills;

import com.cavetale.skills.command.CommandContext;
import com.cavetale.skills.command.CommandNode;
import com.cavetale.skills.command.CommandWarn;
import com.winthier.generic_events.GenericEvents;
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
    final CommandNode root = new CommandNode("skills");

    enum HighscoreType {
        TOTAL,
        TALENTS;

        public final String key;
        public final String displayName;

        HighscoreType() {
            key = name().toLowerCase();
            displayName = Msg.enumToCamelCase(this);
        }
    }

    private String commandHelp(String cmd, String args, String desc) {
        return ""
            + ChatColor.YELLOW + "/sk "
            + ChatColor.GOLD + cmd
            + (args == null
               ? ""
               : " " + ChatColor.YELLOW + ChatColor.ITALIC + args)
            + (desc == null
               ? ""
               : ChatColor.DARK_GRAY + " - " + ChatColor.WHITE + desc);
    }

    void enable() {
        root.helpLine(ChatColor.YELLOW + "/sk" + ChatColor.DARK_GRAY + " - " + ChatColor.WHITE + "Skills Command Interface");
        root.addChild("list")
            .terminal()
            .caller((ctx, nod, args) -> listCommand(ctx.requirePlayer(), args))
            .helpLine(commandHelp("list", null, "List your skills and talents"));
        CommandNode talentNode = root.addChild("talent")
            .caller(this::talentCommand)
            .helpLine(commandHelp("talent", null, "Talent menu"));
        talentNode.addChild("unlock")
            .caller(this::talentUnlockCommand)
            .completer(this::talentUnlockComplete);
        root.addChild("info")
            .caller(this::infoCommand)
            .completionList(this::infoCompletionList)
            .helpLine(commandHelp("info", null, "Info pages"));
        CommandNode highscoreNode = root.addChild("highscore")
            .alias("hi")
            .caller(this::highscoreCommand)
            .helpLine(commandHelp("hi", null, "Highscore lists"));
        for (SkillType skillType : SkillType.values()) {
            root.addChild(skillType.key)
                .terminal()
                .caller((ctx, nod, args) -> skillCommand(ctx.requirePlayer(), skillType, args))
                .helpLine(commandHelp(skillType.key, null, skillType.displayName + " skill menu"));
            highscoreNode.addChild(skillType.key)
                .terminal()
                .caller((ctx, nod, args) -> highscoreFinalCommand(ctx.requirePlayer(), args, skillType, null))
                .helpLine(commandHelp("hi " + skillType.key, null, skillType.displayName + " highscores"));
        }
        for (HighscoreType highscoreType : HighscoreType.values()) {
            highscoreNode.addChild(highscoreType.key)
                .terminal()
                .caller((ctx, nod, args) -> highscoreFinalCommand(ctx.requirePlayer(), args, null, highscoreType))
                .helpLine(commandHelp("hi " + highscoreType.key, null, highscoreType.displayName + " highscores"));
        }
        plugin.getCommand("skills").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        return root.call(new CommandContext(sender, command, alias, args), args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return root.complete(new CommandContext(sender, command, alias, args), args);
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

    boolean infoCommand(CommandContext context, CommandNode node, String[] args) {
        Player player = context.requirePlayer();
        if (args.length > 1) return false;
        if (args.length == 1) {
            Info info = plugin.infos.get(args[0]);
            if (info == null) {
                throw new CommandWarn("Not found: " + args[0]);
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

    List<String> infoCompletionList(CommandContext context) {
        return plugin.infos.allKeys();
    }

    boolean talentCommand(CommandContext context, CommandNode node, String[] args) {
        if (args.length != 0) return false;
        talentMenu(context.requirePlayer());
        return true;
    }

    List<String> talentUnlockComplete(CommandContext context, CommandNode node, String[] args) {
        if (args.length == 0) return null;
        Player player = context.requirePlayer();
        Session session = plugin.sessions.of(player);
        String arg = args[args.length - 1].toLowerCase();
        return Stream.of(Talent.values())
            .filter(t -> !session.hasTalent(t) && session.canAccessTalent(t))
            .map(t -> t.key)
            .filter(s -> s.startsWith(arg))
            .collect(Collectors.toList());
    }

    boolean talentUnlockCommand(CommandContext context, CommandNode node, String[] args) {
        Player player = context.requirePlayer();
        if (args.length != 1) return false;
        Session session = plugin.sessions.of(player);
        if (session.getTalentPoints() < session.getTalentCost()) {
            throw new CommandWarn("You don't have enough Talent Points!");
        }
        Talent talent = Talent.of(args[0]);
        if (talent == null) {
            throw new CommandWarn("Invalid talent!");
        }
        if (session.hasTalent(talent)) {
            throw new CommandWarn("Already unlocked!");
        }
        if (!session.canAccessTalent(talent)) {
            throw new CommandWarn("Parent not yet available!");
        }
        if (!plugin.talents.unlock(player, talent)) {
            throw new CommandWarn("An unknown error occured.");
        }
        Effects.talentUnlock(player);
        talentMenu(player);
        return true;
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

    boolean highscoreCommand(CommandContext context, CommandNode node, String[] args) {
        Player player = context.requirePlayer();
        if (args.length != 0) return false;
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

    boolean highscoreFinalCommand(Player player, String[] args, SkillType skillType, HighscoreType highscoreType) {
        if (args.length > 1) return false;
        // Page Number
        int page = 0;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]) - 1;
            } catch (NumberFormatException nfe) {
                page = -1;
            }
            if (page < 0) {
                throw new CommandWarn("Invalid page number: " + args[0]);
            }
        }
        // Collect
        List<Score> scores;
        final String title;
        if (highscoreType != null) {
            switch (highscoreType) {
            case TOTAL:
                title = "Total";
                scores = plugin.sql.playerRows.values().stream()
                    .filter(p -> p.levels > 0)
                    .map(p -> new Score(p.levels, p.uuid))
                    .collect(Collectors.toList());
                break;
            case TALENTS:
                title = "Talents";
                scores = plugin.sql.playerRows.values().stream()
                    .filter(p -> p.talents > 0)
                    .map(p -> new Score(p.talents, p.uuid))
                    .collect(Collectors.toList());
                break;
            default:
                throw new IllegalStateException("highscoreType=" + highscoreType);
            }
        } else if (skillType != null) {
            title = skillType.displayName;
            scores = plugin.sql.skillRows.stream()
                .filter(s -> s.level > 0)
                .filter(s -> skillType.key.equals(s.skill))
                .map(s -> new Score(s.level, s.player))
                .collect(Collectors.toList());
        } else {
            throw new IllegalStateException("highscoreType == null && skillType == null");
        }
        int offset = page * 10;
        if (offset >= scores.size()) {
            throw new CommandWarn("Page " + (page + 1) + " unavailable!");
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

    void openSkillsMenu(Player player) {
        Gui gui = new Gui(plugin)
            .rows(3)
            .title(ChatColor.DARK_BLUE + "Skills Mk2");
        for (SkillType skillType : SkillType.values()) {
            gui.setItem(skillType.guiIndex, skillType.getIcon(player), click -> {
                    Gui.click(player);
                    openSkillMenu(player, skillType);
                    return true;
                });
        }
        gui.open(player);
    }

    void openSkillMenu(Player player, SkillType skillType) {
        Gui gui = new Gui(plugin)
            .rows(3)
            .title(ChatColor.DARK_PURPLE + skillType.displayName);
        gui.setItem(-999, null, click -> {
                Gui.click(player);
                openSkillsMenu(player);
                return true;
            });
        gui.setItem(4, 0, skillType.getIcon(player), click -> {
                return false;
            });
        gui.open(player);
    }
}
