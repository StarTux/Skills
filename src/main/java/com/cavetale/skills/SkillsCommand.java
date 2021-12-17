package com.cavetale.skills;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.font.DefaultFont;
import com.cavetale.skills.info.Info;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.util.Books;
import com.winthier.playercache.PlayerCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

public final class SkillsCommand extends AbstractCommand<SkillsPlugin> {
    protected final CommandArgCompleter[] highscoreCompleters = new CommandArgCompleter[] {
        CommandArgCompleter.supplyStream(() -> {
                return Stream.concat(Stream.of("total", "talents"),
                                     Stream.of(SkillType.values())
                                     .map(SkillType::getKey));
            }),
        CommandArgCompleter.integer(i -> i > 0),
    };

    protected SkillsCommand(final SkillsPlugin plugin) {
        super(plugin, "skills");
    }

    @Override
    protected void onEnable() {
        for (SkillType skillType : SkillType.values()) {
            rootNode.addChild(skillType.key).denyTabCompletion()
                .description(skillType.displayName + " Skill")
                .playerCaller((player, args) -> skill(player, skillType, args));
        }
        rootNode.addChild("list").denyTabCompletion()
            .description("List all skills")
            .playerCaller(this::list);
        rootNode.addChild("info").arguments("<page>")
            .description("View info page")
            .completers(CommandArgCompleter.supplyList(() -> List.copyOf(plugin.infos.keys())))
            .playerCaller(this::info);
        rootNode.addChild("talent").denyTabCompletion()
            .description("Talent Menu")
            .playerCaller(this::talent);
        rootNode.addChild("hi").arguments("[skill] [page]")
            .description("Highscore List")
            .completers(highscoreCompleters)
            .playerCaller(this::hi);
        rootNode.addChild("upgrade").hidden(true)
            .playerCaller(this::upgrade);
    }

    protected Component prop(String left, String right) {
        return Component.join(JoinConfiguration.noSeparators(),
                              Component.text(left, NamedTextColor.GRAY),
                              Component.text(right));
    }

    protected boolean skill(Player player, SkillType skillType, String[] args) {
        if (args.length != 0) return false;
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) {
            throw new CommandWarn("Session not ready. Please try again later!");
        }
        int level = session.getLevel(skillType);
        int points = session.getSkillPoints(skillType);
        int req = plugin.pointsForLevelUp(level + 1);
        long talentCount = Stream.of(TalentType.values())
            .filter(t -> t.skillType == skillType).count();
        long talentsHas = Stream.of(TalentType.values())
            .filter(t -> t.skillType == skillType)
            .filter(session::hasTalent).count();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text(skillType.displayName, skillType.tag.color(), TextDecoration.BOLD));
        lines.add(Component.empty());
        Info info = plugin.infos.get(skillType.key);
        if (info != null) {
            lines.add(Component.text(info.description.split("\n\n")[0], NamedTextColor.GRAY));
        }
        lines.add(Component.empty());
        lines.add(prop("Level ", "" + level));
        lines.add(prop("Exp Bonus ", "" + session.getExpBonus(skillType)));
        lines.add(prop("Points ", points + "/" + req));
        lines.add(prop("Talents ", talentsHas + "/" + talentCount));
        if (talentsHas < talentCount) {
            int talentPoints = session.getTalentPoints(skillType);
            int talentCost = session.getTalentCost(skillType);
            lines.add(prop("Talent Points ", talentPoints + "/" + talentCost));
        }
        if (points >= req) {
            lines.add(Component.join(JoinConfiguration.noSeparators(),
                                     Component.text("Upgrade to Level " + (level + 1) + "? "),
                                     DefaultFont.YES_BUTTON.component
                                     .hoverEvent(HoverEvent.showText(Component.text("Yes", NamedTextColor.BLUE)))
                                     .clickEvent(ClickEvent.runCommand("/sk upgrade " + skillType.key))));
        }
        Books.open(player, List.of(Component.join(JoinConfiguration.separator(Component.newline()), lines)));
        return true;
    }

    protected boolean list(@NonNull Player player, String[] args) {
        if (args.length != 0) return false;
        List<Component> lines = new ArrayList<>();
        lines.add(Component.empty());
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) {
            throw new CommandWarn("Session not ready. Please try again later!");
        }
        for (SkillType skill : SkillType.values()) {
            int level = session.getLevel(skill);
            int points = session.getSkillPoints(skill);
            int req = plugin.pointsForLevelUp(level + 1);
            lines.add(Component.join(JoinConfiguration.noSeparators(),
                                     Component.text("lvl", NamedTextColor.DARK_PURPLE),
                                     Component.text(level, NamedTextColor.YELLOW, TextDecoration.BOLD),
                                     Component.text(" " + skill.displayName, NamedTextColor.LIGHT_PURPLE),
                                     Component.text(" " + points, NamedTextColor.WHITE),
                                     Component.text("/", NamedTextColor.LIGHT_PURPLE),
                                     Component.text(req, NamedTextColor.WHITE)));
        }
        lines.add(Component.join(JoinConfiguration.builder()
                                 .prefix(Component.text("Talents: ", NamedTextColor.LIGHT_PURPLE))
                                 .separator(Component.text(", ", NamedTextColor.DARK_PURPLE))
                                 .build(),
                                 Stream.of(TalentType.values())
                                 .filter(session::hasTalent)
                                 .map(t -> Component.text(t.tag.title(), NamedTextColor.GOLD))
                                 .collect(Collectors.toList())));
        player.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()), lines));
        return true;
    }

    protected boolean info(@NonNull Player player, String[] args) {
        if (args.length > 1) return false;
        if (args.length == 0) {
            Component msg = Component.join(JoinConfiguration.builder()
                                           .prefix(Component.text("Pages: ", NamedTextColor.LIGHT_PURPLE))
                                           .separator(Component.text(", ", NamedTextColor.DARK_PURPLE))
                                           .build(),
                                           plugin.infos.keys().stream()
                                           .map(s -> Component.text(s, NamedTextColor.YELLOW)
                                                .clickEvent(ClickEvent.runCommand("/sk info " + s))
                                                .hoverEvent(HoverEvent.showText(Component.text(plugin.infos.get(s).title,
                                                                                               NamedTextColor.YELLOW,
                                                                                               TextDecoration.BOLD))))
                                           .collect(Collectors.toList()));
            player.sendMessage(msg);
            return true;
        }
        Info info = plugin.infos.get(args[0]);
        if (info == null) {
            throw new CommandWarn("Not found: " + args[0]);
        }
        List<Component> lines = List.of(Component.text(info.title, NamedTextColor.YELLOW, TextDecoration.BOLD),
                                        Component.text(info.category, NamedTextColor.DARK_GRAY, TextDecoration.ITALIC),
                                        Component.text(info.description, NamedTextColor.WHITE));
        player.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()), lines));
        return true;
    }

    protected boolean talent(@NonNull Player player, String[] args) {
        if (args.length != 0) return false;
        plugin.guis.talents(player);
        return true;
    }

    @Value
    protected static final class Score implements Comparable<Score> {
        protected final int score;
        protected final UUID uuid;

        @Override
        public int compareTo(Score o) {
            return Integer.compare(o.score, score);
        }
    }

    protected boolean hi(@NonNull Player player, String[] args) {
        if (args.length == 0) {
            List<Component> cb = new ArrayList<>();
            cb.add(Component.text("Options: ", NamedTextColor.LIGHT_PURPLE));
            String cmd = "/sk hi total";
            cb.add(Component.text("Total", NamedTextColor.YELLOW)
                   .clickEvent(ClickEvent.runCommand(cmd))
                   .hoverEvent(HoverEvent.showText(Component.text(cmd, NamedTextColor.YELLOW))));
            cb.add(Component.text(", ", NamedTextColor.DARK_PURPLE));
            cmd = "/sk hi talents";
            cb.add(Component.text("Talents", NamedTextColor.YELLOW)
                   .clickEvent(ClickEvent.runCommand(cmd))
                   .hoverEvent(HoverEvent.showText(Component.text(cmd, NamedTextColor.YELLOW))));
            for (SkillType skill : SkillType.values()) {
                cb.add(Component.text(", ", NamedTextColor.DARK_PURPLE));
                cmd = "/sk hi " + skill.key;
                cb.add(Component.text(skill.displayName, NamedTextColor.GOLD)
                       .clickEvent(ClickEvent.runCommand(cmd))
                       .hoverEvent(HoverEvent.showText(Component.text(cmd, NamedTextColor.GOLD))));
            }
            player.sendMessage(Component.join(JoinConfiguration.noSeparators(), cb));
            return true;
        }
        if (args.length > 2) return false;
        // Skill
        SkillType skill = SkillType.ofKey(args[0]);
        if (!"total".equals(args[0]) && !"talents".equals(args[0]) && skill == null) {
            throw new CommandWarn("Invalid skill: " + args[0]);
        }
        // Page Number
        final int page;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]) - 1;
            } catch (NumberFormatException nfe) {
                throw new CommandWarn("Invalid page number: " + args[1]);
            }
            if (page < 0) {
                throw new CommandWarn("Invalid page number: " + page);
            }
        } else {
            page = 0;
        }
        if (skill == null) {
            if (args[0].equals("total")) {
                plugin.database.find(SQLPlayer.class).findListAsync(rows -> {
                        highscoreCallback(player,
                                          rows.stream()
                                          .filter(p -> p.getLevels() > 0)
                                          .map(p -> new Score(p.getLevels(), p.getUuid()))
                                          .collect(Collectors.toList()),
                                          page, "Total");
                    });
            } else if (args[0].equals("talents")) {
                plugin.database.find(SQLPlayer.class).findListAsync(rows -> {
                        highscoreCallback(player,
                                          rows.stream()
                                          .filter(p -> p.getTalents() > 0)
                                          .map(p -> new Score(p.getTalents(), p.getUuid()))
                                          .collect(Collectors.toList()),
                                          page, "Talents");
                    });
            } else {
                throw new IllegalStateException("arg=" + args[0]);
            }
        } else {
            plugin.database.find(SQLSkill.class).findListAsync(rows -> {
                    highscoreCallback(player,
                                      rows.stream()
                                      .filter(s -> s.getLevel() > 0)
                                      .filter(s -> skill.key.equals(s.getSkill()))
                                      .map(s -> new Score(s.getLevel(), s.getPlayer()))
                                      .collect(Collectors.toList()),
                                      page, skill.displayName);
                });
        }
        return true;
    }

    protected void highscoreCallback(Player player, List<Score> scores, int page, String title) {
        int offset = page * 10;
        if (offset >= scores.size()) {
            throw new CommandWarn("Page " + (page + 1) + " unavailable!");
        }
        Collections.sort(scores);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.empty());
        lines.add(Component.text(title + " Highscore", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
        for (int i = 0; i < 10; i += 1) {
            final int index = offset + i;
            final int rank = index + 1;
            final String level;
            final String name;
            if (index < scores.size()) {
                Score score = scores.get(index);
                level = "" + score.score;
                name = PlayerCache.nameForUuid(score.uuid);
            } else {
                level = "?";
                name = " ---";
            }
            lines.add(Component.join(JoinConfiguration.noSeparators(),
                                     Component.text("#" + rank, NamedTextColor.DARK_PURPLE),
                                     Component.text(" " + level, NamedTextColor.WHITE),
                                     Component.text(" " + name, NamedTextColor.LIGHT_PURPLE)));
        }
        player.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()),
                                          lines));
    }

    /**
     * Click command!
     */
    protected boolean upgrade(Player player, String[] args) {
        if (args.length != 1) return true;
        SkillType skillType = SkillType.ofKey(args[0]);
        if (skillType == null) return true;
        plugin.sessions.apply(player, session -> {
                session.levelUp(skillType);
            });
        return true;
    }
}
