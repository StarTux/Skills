package com.cavetale.skills;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandWarn;
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
import org.bukkit.entity.Player;
import static com.cavetale.core.font.Unicode.tiny;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

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
    }

    protected Component prop(String left, String right) {
        return join(JoinConfiguration.noSeparators(),
                    text(tiny(left.toLowerCase()), GRAY),
                    text(right));
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
        lines.add(text(skillType.displayName, skillType.tag.color(), BOLD));
        lines.add(text(skillType.tag.description()));
        lines.add(prop("Level ", "" + level));
        lines.add(prop("Exp Bonus ", "" + session.getExpBonus(skillType)));
        lines.add(prop("Money Bonus ", "" + SkillsPlugin.moneyBonusPercentage(session.getMoneyBonus(skillType)) + "%"));
        lines.add(prop("Points ", points + "/" + req));
        lines.add(prop("Talents ", talentsHas + "/" + talentCount));
        if (talentsHas < talentCount) {
            int talentPoints = session.getTalentPoints(skillType);
            lines.add(prop("Talent Points ", "" + talentPoints));
        }
        Books.open(player, List.of(join(JoinConfiguration.separator(newline()), lines)));
        return true;
    }

    protected boolean list(@NonNull Player player, String[] args) {
        if (args.length != 0) return false;
        List<Component> lines = new ArrayList<>();
        lines.add(empty());
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) {
            throw new CommandWarn("Session not ready. Please try again later!");
        }
        for (SkillType skill : SkillType.values()) {
            int level = session.getLevel(skill);
            int points = session.getSkillPoints(skill);
            int req = plugin.pointsForLevelUp(level + 1);
            lines.add(join(JoinConfiguration.noSeparators(),
                           text("lvl", DARK_PURPLE),
                           text(level, YELLOW, BOLD),
                           text(" " + skill.displayName, LIGHT_PURPLE),
                           text(" " + points, WHITE),
                           text("/", LIGHT_PURPLE),
                           text(req, WHITE)));
        }
        lines.add(join(JoinConfiguration.builder()
                       .prefix(text("Talents: ", LIGHT_PURPLE))
                       .separator(text(", ", DARK_PURPLE))
                       .build(),
                       Stream.of(TalentType.values())
                       .filter(session::hasTalent)
                       .map(t -> text(t.tag.title(), GOLD))
                       .collect(Collectors.toList())));
        player.sendMessage(join(JoinConfiguration.separator(newline()), lines));
        return true;
    }

    protected boolean info(@NonNull Player player, String[] args) {
        if (args.length > 1) return false;
        if (args.length == 0) {
            Component msg = join(JoinConfiguration.builder()
                                 .prefix(text("Pages: ", LIGHT_PURPLE))
                                 .separator(text(", ", DARK_PURPLE))
                                 .build(),
                                 plugin.infos.keys().stream()
                                 .map(s -> text(s, YELLOW)
                                      .clickEvent(runCommand("/sk info " + s))
                                      .hoverEvent(showText(text(plugin.infos.get(s).title,
                                                                YELLOW,
                                                                BOLD))))
                                 .collect(Collectors.toList()));
            player.sendMessage(msg);
            return true;
        }
        Info info = plugin.infos.get(args[0]);
        if (info == null) {
            throw new CommandWarn("Not found: " + args[0]);
        }
        List<Component> lines = new ArrayList<>();
        lines.add(text(info.title, YELLOW, BOLD));
        lines.add(text(info.category, DARK_GRAY, ITALIC));
        lines.addAll(info.pages);
        player.sendMessage(join(JoinConfiguration.separator(newline()), lines));
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
            cb.add(text("Options: ", LIGHT_PURPLE));
            String cmd = "/sk hi total";
            cb.add(text("Total", YELLOW)
                   .clickEvent(runCommand(cmd))
                   .hoverEvent(showText(text(cmd, YELLOW))));
            cb.add(text(", ", DARK_PURPLE));
            cmd = "/sk hi talents";
            cb.add(text("Talents", YELLOW)
                   .clickEvent(runCommand(cmd))
                   .hoverEvent(showText(text(cmd, YELLOW))));
            for (SkillType skill : SkillType.values()) {
                cb.add(text(", ", DARK_PURPLE));
                cmd = "/sk hi " + skill.key;
                cb.add(text(skill.displayName, GOLD)
                       .clickEvent(runCommand(cmd))
                       .hoverEvent(showText(text(cmd, GOLD))));
            }
            player.sendMessage(join(JoinConfiguration.noSeparators(), cb));
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
            player.sendMessage(text("Page " + (page + 1) + " unavailable!", RED));
            return;
        }
        Collections.sort(scores);
        List<Component> lines = new ArrayList<>();
        lines.add(empty());
        lines.add(text(title + " Highscore", LIGHT_PURPLE, BOLD));
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
            lines.add(join(JoinConfiguration.noSeparators(),
                           text("#" + rank, DARK_PURPLE),
                           text(" " + level, WHITE),
                           text(" " + name, LIGHT_PURPLE)));
        }
        player.sendMessage(join(JoinConfiguration.separator(newline()),
                                lines));
    }
}
