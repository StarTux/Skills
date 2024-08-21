package com.cavetale.skills;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.core.playercache.PlayerCache;
import com.cavetale.mytems.Mytems;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.util.Books;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.Value;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import static com.cavetale.core.font.Unicode.subscript;
import static com.cavetale.core.font.Unicode.superscript;
import static com.cavetale.core.font.Unicode.tiny;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.JoinConfiguration.separator;
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
                .playerCaller((player) -> skill(player, skillType));
        }
        rootNode.playerCaller(this::list);
        rootNode.addChild("list").denyTabCompletion()
            .description("List all skills")
            .playerCaller(this::list);
        rootNode.addChild("talent").denyTabCompletion()
            .description("Talent Menu")
            .playerCaller(this::talent);
        rootNode.addChild("hi").arguments("<skill>")
            .description("Highscore List")
            .completers(highscoreCompleters)
            .playerCaller(this::hi);
    }

    protected Component prop(String left, String right, String cmd) {
        return textOfChildren(text(tiny(left.toLowerCase()), GRAY),
                              text(right))
            .hoverEvent(showText(text(cmd, GRAY)))
            .clickEvent(runCommand(cmd));
    }

    protected Component prop(String left, String right) {
        return textOfChildren(text(tiny(left.toLowerCase()), GRAY),
                              text(right));
    }

    protected void skill(Player player, SkillType skillType) {
        Session session = Session.of(player);
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
        List<Component> description = skillType.getDescription();
        lines.add(skillType.getIconTitle()
                  .hoverEvent(showText(join(separator(newline()),
                                            text("/sk list", GRAY),
                                            textOfChildren(Mytems.MOUSE_LEFT, text(" Back to List", GRAY)))))
                  .clickEvent(runCommand("/sk list")));
        lines.add(empty());
        lines.add(textOfChildren(VanillaItems.ENDER_EYE, text(" Talents", skillType.textColor))
                  .hoverEvent(showText(join(separator(newline()),
                                            text("/tal " + skillType.key, skillType.textColor),
                                            textOfChildren(Mytems.MOUSE_LEFT, text(" Open Talent Menu", GRAY)))))
                  .clickEvent(runCommand("/tal " + skillType.key)));
        lines.add(description.get(0));
        lines.add(empty());
        lines.add(prop("Level ", "" + level));
        lines.add(prop("Exp Bonus ", "" + session.getExpBonus(skillType), "/tal " + skillType.key));
        lines.add(prop("Money Bonus ", "" + SkillsPlugin.moneyBonusPercentage(session.getMoneyBonus(skillType)) + "%", "/tal " + skillType.key));
        lines.add(prop("Points ", points + "/" + req));
        lines.add(prop("Talents ", talentsHas + "/" + talentCount, "/tal " + skillType.key));
        if (talentsHas < talentCount) {
            int talentPoints = session.getTalentPoints(skillType);
            lines.add(prop("Talent Points ", "" + talentPoints, "/tal " + skillType.key));
        }
        List<Component> pages = new ArrayList<>();
        pages.add(join(separator(newline()), lines));
        for (int i = 1; i < description.size(); i += 1) {
            pages.add(description.get(i));
        }
        Books.open(player, pages);
    }

    protected boolean list(@NonNull Player player, String[] args) {
        if (args.length != 0) return false;
        List<Component> lines = new ArrayList<>();
        lines.add(text("Skills Mk2", DARK_BLUE));
        Session session = Session.of(player);
        if (!session.isEnabled()) {
            throw new CommandWarn("Session not ready. Please try again later!");
        }
        lines.add(empty());
        for (SkillType skill : SkillType.values()) {
            int level = session.getLevel(skill);
            int points = session.getSkillPoints(skill);
            int req = plugin.pointsForLevelUp(level + 1);
            int talents = session.getTalentCount(skill);
            int talentPoints = session.getTalentPoints(skill);
            String cmd = "/sk " + skill.key;
            String cmd2 = "/tal " + skill.key;
            List<Component> skillTooltip = new ArrayList<>();
            skillTooltip.add(skill.getIconTitle());
            skillTooltip.add(text(cmd, skill.textColor));
            skillTooltip.add(empty());
            skillTooltip.add(prop("Level ", "" + level));
            skillTooltip.add(prop("Points ", superscript(points) + "/" + subscript(req)));
            skillTooltip.add(empty());
            skillTooltip.add(textOfChildren(Mytems.MOUSE_LEFT, text(" Details", GRAY)));
            List<Component> talentTooltip = new ArrayList<>();
            talentTooltip.add(skill.getIconTitle());
            talentTooltip.add(text(cmd2, skill.textColor));
            talentTooltip.add(empty());
            talentTooltip.add(prop("Talents ", "" + talents));
            talentTooltip.add(prop("Talent Points ", "" + talentPoints));
            talentTooltip.add(empty());
            talentTooltip.add(textOfChildren(Mytems.MOUSE_LEFT, text(" Details", GRAY)));
            lines.add(textOfChildren(textOfChildren(skill.getIconTitle(),
                                                    space(),
                                                    text(tiny("lvl"), GRAY),
                                                    text(level))
                                     .hoverEvent(showText(join(separator(newline()), skillTooltip)))
                                     .clickEvent(runCommand(cmd)),
                                     textOfChildren(space(),
                                                    text(tiny("tal"), GRAY),
                                                    text(talents))
                                     .hoverEvent(showText(join(separator(newline()), talentTooltip)))
                                     .clickEvent(runCommand(cmd2))));
        }
        lines.add(space());
        lines.add(textOfChildren(Mytems.GOLDEN_CUP, text("Highscores", DARK_BLUE))
                  .hoverEvent(showText(text("/hi", GRAY)))
                  .clickEvent(runCommand("/hi")));
        Books.open(player, List.of(join(separator(newline()), lines)));
        return true;
    }

    protected boolean talent(@NonNull Player player, String[] args) {
        final Session session = Session.of(player);
        if (!session.isEnabled()) {
            throw new CommandWarn("Session not ready. Please try again later!");
        }
        if (args.length == 1) {
            SkillType skillType = CommandArgCompleter.requireEnum(SkillType.class, args[0].toUpperCase());
            session.setTalentGui(skillType);
        } else if (args.length != 0) {
            return false;
        }
        new TalentMenu(player, session).open();
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
            List<Component> lines = new ArrayList<>();
            lines.add(text("Highscores", DARK_BLUE, BOLD)
                      .hoverEvent(showText(text("/sk list", GRAY)))
                      .clickEvent(runCommand("/sk list")));
            lines.add(empty());
            String cmd = "/hi total";
            lines.add(textOfChildren(Mytems.GOLDEN_CUP, text("Total", DARK_BLUE))
                      .clickEvent(runCommand(cmd))
                      .hoverEvent(showText(text(cmd, GRAY))));
            lines.add(empty());
            cmd = "/hi talents";
            lines.add(textOfChildren(VanillaItems.ENDER_EYE, text("Talents", DARK_PURPLE))
                      .clickEvent(runCommand(cmd))
                      .hoverEvent(showText(text(cmd, GRAY))));
            for (SkillType skill : SkillType.values()) {
                lines.add(empty());
                cmd = "/hi " + skill.key;
                lines.add(skill.getIconTitle()
                          .clickEvent(runCommand(cmd))
                          .hoverEvent(showText(text(cmd, skill.textColor))));
            }
            Books.open(player, List.of(join(separator(newline()), lines)));
            return true;
        }
        if (args.length > 1) return false;
        // Skill
        SkillType skill = SkillType.ofKey(args[0]);
        if (!"total".equals(args[0]) && !"talents".equals(args[0]) && skill == null) {
            throw new CommandWarn("Invalid skill: " + args[0]);
        }
        if (skill == null) {
            if (args[0].equals("total")) {
                plugin.database.find(SQLPlayer.class).findListAsync(rows -> {
                        highscoreCallback(player,
                                          rows.stream()
                                          .filter(p -> p.getLevels() > 0)
                                          .map(p -> new Score(p.getLevels(), p.getUuid()))
                                          .collect(Collectors.toList()),
                                          "Total");
                    });
            } else if (args[0].equals("talents")) {
                plugin.database.find(SQLPlayer.class).findListAsync(rows -> {
                        highscoreCallback(player,
                                          rows.stream()
                                          .filter(p -> p.getTalents() > 0)
                                          .map(p -> new Score(p.getTalents(), p.getUuid()))
                                          .collect(Collectors.toList()),
                                          "Talents");
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
                                      skill.displayName);
                });
        }
        return true;
    }

    protected void highscoreCallback(Player player, List<Score> scores, String title) {
        if (scores.isEmpty()) {
            player.sendMessage(text("No highscores to show", RED));
            return;
        }
        Collections.sort(scores);
        List<Component> pages = new ArrayList<>();
        int rank = 0;
        int oldScore = -1;
        for (int offset = 0; offset < scores.size(); offset += 10) {
            List<Component> lines = new ArrayList<>();
            lines.add(text(title + " Highscore", DARK_BLUE, BOLD)
                      .hoverEvent(showText(text("/hi", GRAY)))
                      .clickEvent(runCommand("/hi")));
            for (int line = 0; line < 10; line += 1) {
                if (offset + line >= scores.size()) break;
                Score row = scores.get(offset + line);
                if (oldScore != row.score) {
                    oldScore = row.score;
                    rank += 1;
                }
                lines.add(textOfChildren(text(rank, BLUE, BOLD),
                                         text(subscript(row.score), GRAY),
                                         space(),
                                         text("" + PlayerCache.nameForUuid(row.uuid))));
            }
            pages.add(join(separator(newline()), lines));
        }
        Books.open(player, pages);
    }
}
