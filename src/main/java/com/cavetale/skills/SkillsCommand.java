package com.cavetale.skills;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.util.Books;
import com.cavetale.skills.util.Effects;
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

final class SkillsCommand extends AbstractCommand<SkillsPlugin> {
    protected SkillsCommand(final SkillsPlugin plugin) {
        super(plugin, "skills");
    }

    @Override
    public void onEnable() {
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
            .completers(CommandArgCompleter.supplyList(() -> List.copyOf(plugin.infos.keySet())))
            .playerCaller(this::info);
        rootNode.addChild("talent").denyTabCompletion()
            .description("TalentType menu")
            .playerCaller(this::talent);
        rootNode.addChild("hi").arguments("[skill] [page]")
            .description("Highscore List")
            .completers(new CommandArgCompleter[] {
                    CommandArgCompleter.supplyStream(() -> {
                            return Stream.concat(Stream.of("total", "talents"),
                                                 Stream.of(SkillType.values())
                                                 .map(SkillType::getKey));
                        }),
                    CommandArgCompleter.integer(i -> i > 0),
                })
            .playerCaller(this::hi);
    }

    protected Component prop(String left, String right) {
        return Component.join(JoinConfiguration.noSeparators(),
                              Component.text(left, NamedTextColor.GRAY),
                              Component.text(right));
    }

    protected boolean skill(Player player, @NonNull SkillType skill, String[] args) {
        if (args.length != 0) return false;
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) {
            throw new CommandWarn("Session not ready. Please try again later!");
        }
        int level = session.getLevel(skill);
        int points = session.getSkillPoints(skill);
        int req = plugin.pointsForLevelUp(level + 1);
        long talents = Stream.of(TalentType.values())
            .filter(t -> t.skillType == skill).count();
        long talentsHas = Stream.of(TalentType.values())
            .filter(t -> t.skillType == skill)
            .filter(session::hasTalent).count();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text(skill.displayName, NamedTextColor.GOLD, TextDecoration.BOLD));
        lines.add(Component.empty());
        Info info = plugin.infos.get(skill.key);
        if (info != null) {
            lines.add(Component.text(info.description.split("\n\n")[0], NamedTextColor.GRAY));
        }
        lines.add(Component.empty());
        lines.add(prop("Level ", "" + level));
        lines.add(prop("Exp Bonus ", "" + session.getExpBonus(skill)));
        lines.add(prop("Points ", points + "/" + req));
        lines.add(prop("Talents ", talentsHas + "/" + talents));
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
                                 .map(e -> Component.text(plugin.getTalentInfo(e.key).title, NamedTextColor.GOLD))
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
                                           plugin.infos.keySet().stream()
                                           .map(s -> Component.text(s, NamedTextColor.YELLOW))
                                           .collect(Collectors.toList()));
            player.sendMessage(msg);
            return true;
        }
        Info info = plugin.infos.get(args[0]);
        if (info == null) {
            throw new CommandWarn("Not found: " + args[0]);
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text(info.title, NamedTextColor.YELLOW, TextDecoration.BOLD));
        for (String p : info.description.split("\n\n")) {
            lines.add(Component.empty());
            lines.add(Component.text(p));
        }
        player.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()), lines));
        return true;
    }

    protected boolean talent(@NonNull Player player, String[] args) {
        if (args.length == 0) {
            talentMenu(player);
            return true;
        }
        switch (args[0]) {
        case "unlock": {
            if (args.length != 2) return false;
            Session session = plugin.sessions.of(player);
            if (!session.isEnabled()) {
                throw new CommandWarn("Session not ready. Please try again later!");
            }
            if (session.getTalentPoints() < session.getTalentCost()) {
                throw new CommandWarn("You don't have enough TalentType Points!");
            }
            TalentType talent = TalentType.of(args[1]);
            if (talent == null) {
                throw new CommandWarn("Invalid talent!");
            }
            if (session.hasTalent(talent)) {
                throw new CommandWarn("Already unlocked!");
            }
            if (!session.canAccessTalent(talent)) {
                throw new CommandWarn("Parent not yet available!");
            }
            if (!session.unlockTalent(talent)) {
                throw new CommandWarn("An unknown error occured.");
            }
            Effects.talentUnlock(player);
            talentMenu(player);
            return true;
        }
        case "toggle": {
            if (args.length == 1) {
                Session session = plugin.sessions.of(player);
                if (!session.isEnabled()) {
                    throw new CommandWarn("Session not ready. Please try again later!");
                }
                boolean dis = !session.isTalentsDisabled();
                session.setTalentsDisabled(dis);
                player.sendMessage(dis
                                   ? Component.text("Talents disabled", NamedTextColor.RED)
                                   : Component.text("Talents enabled", NamedTextColor.GREEN));
                talentMenu(player);
                return true;
            } else if (args.length == 2) {
                TalentType talent = TalentType.of(args[1]);
                if (talent == null) {
                    throw new CommandWarn("Invalid talent!");
                }
                Session session = plugin.sessions.of(player);
                if (!session.isEnabled()) {
                    throw new CommandWarn("Session not ready. Please try again later!");
                }
                if (!session.hasTalent(talent)) {
                    throw new CommandWarn("You don't have this talent!");
                }
                if (session.getDisabledTalents().contains(talent)) {
                    session.getDisabledTalents().remove(talent);
                    player.sendMessage(Component.text("TalentType enabled: " + talent.displayName,
                                                      NamedTextColor.GREEN));
                } else {
                    session.getDisabledTalents().add(talent);
                    player.sendMessage(Component.text("TalentType disabled: " + talent.displayName,
                                                      NamedTextColor.RED));
                }
                talentMenu(player);
                return true;
            } else {
                return false;
            }
        }
        default: break;
        }
        return false;
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

    public void talentMenu(@NonNull Player player) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text("Skill Talents", NamedTextColor.GOLD, TextDecoration.BOLD));
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) {
            throw new CommandWarn("Session not ready. Please try again later!");
        }
        for (SkillType skillType : SkillType.values()) {
            List<Component> cb = new ArrayList<>();
            cb.add(Component.text(skillType.displayName, NamedTextColor.GRAY));
            for (TalentType talent : TalentType.SKILL_MAP.get(skillType)) {
                TalentInfo info = plugin.getTalentInfo(talent.key);
                Component component;
                NamedTextColor talentColor;
                if (session.hasTalent(talent)) {
                    component = Component.text("(" + info.title + ")",
                                               session.getDisabledTalents().contains(talent) ? NamedTextColor.RED : NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand("/sk talent toggle " + talent.key));
                    talentColor = NamedTextColor.GREEN;
                } else if (session.canAccessTalent(talent) && session.getTalentPoints() >= session.getTalentCost()) {
                    component = Component.text("[" + info.title + "]", NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.runCommand("/sk talent unlock " + talent.key));
                    talentColor = NamedTextColor.GOLD;
                } else {
                    component = Component.text("<" + info.title + ">", NamedTextColor.DARK_GRAY);
                    talentColor = NamedTextColor.GRAY;
                }
                if (session.hasTalent(talent)) {
                    Component tooltip = Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                            Component.text(info.title, NamedTextColor.WHITE),
                            (session.getDisabledTalents().contains(talent)
                             ? (Component.text("Disabled", NamedTextColor.RED)
                                .append(Component.text(" Click to enable", NamedTextColor.GRAY)))
                             : (Component.text("Enabled", NamedTextColor.GREEN)
                                .append(Component.text(" Click to disable", NamedTextColor.GRAY)))),
                            Component.text(info.description, talentColor),
                        });
                    component = component.hoverEvent(HoverEvent.showText(tooltip));
                } else {
                    Component tooltip = Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                            Component.text(info.title, NamedTextColor.WHITE),
                            (talent.depends != null
                             ? Component.text("Requires: " + plugin.getTalentInfo(talent.depends.key).title,
                                              (session.hasTalent(talent.depends) ? NamedTextColor.GREEN : NamedTextColor.DARK_RED))
                             : Component.empty()),
                            Component.text(info.description, talentColor),
                        });
                    component = component.hoverEvent(HoverEvent.showText(tooltip));
                }
                cb.add(component);
            }
            lines.add(Component.join(JoinConfiguration.separator(Component.space()), cb));
        }
        lines.add(prop("TalentType Points ", "" + session.getTalentPoints()));
        lines.add(prop("Unlock Cost ", "" + session.getTalentCost()));
        Component talentComponent = prop("Talents ", session.isTalentsDisabled() ? "Disabled " : "Enabled ");
        if (session.isTalentsDisabled()) {
            talentComponent = talentComponent
                .append(Component.text("[Enable]", NamedTextColor.GREEN)
                        .hoverEvent(HoverEvent.showText(Component.text("Enable Talents", NamedTextColor.GREEN)))
                        .clickEvent(ClickEvent.runCommand("/sk talent toggle")));
        } else {
            talentComponent = talentComponent
                .append(Component.text("[Disable]", NamedTextColor.RED)
                        .hoverEvent(HoverEvent.showText(Component.text("Disable Talents", NamedTextColor.RED)))
                        .clickEvent(ClickEvent.runCommand("/sk talent toggle")));
        }
        lines.add(talentComponent);
        player.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()), lines));
    }
}
