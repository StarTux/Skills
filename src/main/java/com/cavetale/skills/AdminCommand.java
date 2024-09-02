package com.cavetale.skills;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.playercache.PlayerCache;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentLevel;
import com.cavetale.skills.skill.TalentType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static com.cavetale.core.font.Unicode.tiny;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class AdminCommand extends AbstractCommand<SkillsPlugin> {
    protected AdminCommand(final SkillsPlugin plugin) {
        super(plugin, "skilladmin");
    }

    @Override
    protected void onEnable() {
        // Talent
        CommandNode talentNode = rootNode.addChild("talent")
            .description("Talent subcommands");
        talentNode.addChild("info").arguments("<player> <skill>")
            .description("Print talent info")
            .completers(PlayerCache.NAME_COMPLETER,
                        CommandArgCompleter.enumLowerList(SkillType.class))
            .senderCaller(this::talentInfo);
        talentNode.addChild("addpoints").arguments("<player> <skill> <amount>")
            .description("Add Talent Points")
            .completers(PlayerCache.NAME_COMPLETER,
                        CommandArgCompleter.enumLowerList(SkillType.class),
                        CommandArgCompleter.integer(i -> i > 0))
            .senderCaller(this::addTalentPoints);
        talentNode.addChild("printstats").denyTabCompletion()
            .description("Print some talent stats")
            .senderCaller(this::talentPrintStats);
        talentNode.addChild("list").arguments("<skill>")
            .completers(CommandArgCompleter.enumLowerList(SkillType.class))
            .description("List talents")
            .senderCaller(this::talentList);
        // Skills
        CommandNode skillNode = rootNode.addChild("skill")
            .description("Skill subcommands");
        skillNode.addChild("info").arguments("<player>")
            .description("Print player skill info")
            .completers(PlayerCache.NAME_COMPLETER)
            .senderCaller(this::skillInfo);
        skillNode.addChild("addpoints").arguments("<player> <skill> <amount>")
            .description("Add Skill Points")
            .completers(PlayerCache.NAME_COMPLETER,
                        CommandArgCompleter.enumLowerList(SkillType.class),
                        CommandArgCompleter.integer(i -> i != 0))
            .senderCaller(this::addSkillPoints);
        skillNode.addChild("setlevel").arguments("<player> <skill> <level>")
            .description("Set skill level")
            .completers(PlayerCache.NAME_COMPLETER,
                        CommandArgCompleter.enumLowerList(SkillType.class),
                        CommandArgCompleter.integer(i -> i >= 0))
            .senderCaller(this::setSkillLevel);
        // Debug
        final CommandNode debugNode = rootNode.addChild("debug")
            .description("Debug commands");
        debugNode.addChild("skill").arguments("<skill>")
            .description("Toggle debug skills")
            .completers(CommandArgCompleter.enumLowerList(SkillType.class))
            .playerCaller(this::debugSkill);
        debugNode.addChild("talent").arguments("<talent>")
            .description("Toggle debug talents")
            .completers(CommandArgCompleter.enumLowerList(TalentType.class))
            .playerCaller(this::debugTalent);
    }

    private boolean talentInfo(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        PlayerCache target = PlayerCache.require(args[0]);
        final SkillType skillType = SkillType.require(args[1]);
        Session session = plugin.sessions.getOrCreateForAdmin(target.uuid);
        sender.sendMessage(text(target.name + " " + skillType.displayName + " Talent Info", AQUA));
        sender.sendMessage(textOfChildren(text("Talent Points ", GRAY),
                                          text(session.getTalentPoints(skillType)),
                                          text("/", GRAY),
                                          text(session.getTotalTalentPoints(skillType))));
        sender.sendMessage(textOfChildren(text("Money Bonus ", GRAY),
                                          text(session.getMoneyBonus(skillType))));
        sender.sendMessage(textOfChildren(text("Exp Bonus ", GRAY),
                                          text(session.getExpBonus(skillType))));
        List<Component> components = new ArrayList<>();
        for (TalentType talentType : TalentType.SKILL_MAP.get(skillType)) {
            if (session.isTalentEnabled(talentType)) {
                components.add(text(talentType.key, GREEN));
            } else if (session.hasTalent(talentType)) {
                components.add(text(talentType.key, RED, STRIKETHROUGH));
            }
        }
        sender.sendMessage(textOfChildren(text("Talents (" + components.size() + ") ", GRAY),
                                          join(separator(space()), components)));
        return true;
    }

    private boolean addTalentPoints(CommandSender sender, String[] args) {
        if (args.length != 3) return false;
        PlayerCache target = PlayerCache.require(args[0]);
        final SkillType skillType = SkillType.require(args[1]);
        final int amount = CommandArgCompleter.requireInt(args[2], i -> i != 0);
        Session session = plugin.sessions.getOrCreateForAdmin(target.uuid);
        session.getSkill(skillType).modifyTalents(amount, 0, () -> {
                sender.sendMessage(text("Gave " + target.name + " " + amount + " " + skillType.displayName + " TP", YELLOW));
            });
        return true;
    }

    private void talentPrintStats(CommandSender sender) {
        final Map<SkillType, Integer> talentCount = new HashMap<>();
        final Map<SkillType, Integer> levelCount = new HashMap<>();
        final Map<SkillType, Integer> talentPointCost = new HashMap<>();
        for (TalentType talentType : TalentType.values()) {
            talentCount.put(talentType.skillType, talentCount.getOrDefault(talentType.skillType, 0) + 1);
            for (TalentLevel level : talentType.getTalent().getLevels()) {
                levelCount.put(talentType.skillType, levelCount.getOrDefault(talentType.skillType, 0) + 1);
                talentPointCost.put(talentType.skillType, talentPointCost.getOrDefault(talentType.skillType, 0) + level.getTalentPointCost());
            }
        }
        sender.sendMessage("Talents Levels Points");
        for (SkillType skillType : SkillType.values()) {
            final int talents = talentCount.getOrDefault(skillType, 0);
            final int levels = levelCount.getOrDefault(skillType, 0);
            final int cost = talentPointCost.getOrDefault(skillType, 0);
            sender.sendMessage(textOfChildren(text(talents), text(tiny("tal "), GRAY),
                                              text(levels), text(tiny("lvl "), GRAY),
                                              text(cost), text(tiny("tp "), GRAY),
                                              skillType));
        }
    }

    private boolean talentList(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        final SkillType skillType = CommandArgCompleter.requireEnum(SkillType.class, args[0]);
        int totalCost = 0;
        int totalLevels = 0;
        for (TalentType talentType : TalentType.values()) {
            if (talentType.getSkillType() != skillType) continue;
            for (TalentLevel level : talentType.getTalent().getLevels()) {
                totalLevels += 1;
                totalCost += level.getTalentPointCost();
            }
        }
        sender.sendMessage(textOfChildren(text(totalLevels, YELLOW), text(tiny("lvl "), AQUA),
                                          text(totalCost, YELLOW), text(tiny("tp "), AQUA),
                                          skillType));
        for (TalentType talentType : TalentType.values()) {
            if (talentType.getSkillType() != skillType) continue;
            final Talent talent = talentType.getTalent();
            int cost = 0;
            for (TalentLevel level : talent.getLevels()) {
                cost += level.getTalentPointCost();
            }
            sender.sendMessage(textOfChildren(text(talent.getMaxLevel().getLevel()), text(tiny("lvl "), GRAY),
                                              text(cost), text(tiny("tp "), GRAY),
                                              talentType));
        }
        return true;
    }

    private boolean skillInfo(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        PlayerCache target = PlayerCache.require(args[0]);
        Session session = plugin.sessions.getOrCreateForAdmin(target.uuid);
        sender.sendMessage(text(target.name + " Skill Info", AQUA));
        for (SkillType skillType : SkillType.values()) {
            sender.sendMessage(join(separator(space()),
                                    text(skillType.displayName, YELLOW),
                                    text("lvl " + session.getLevel(skillType), AQUA),
                                    text("sp " + session.getSkillPoints(skillType) + "/" + session.getRequiredSkillPoints(skillType), YELLOW),
                                    text("talents " + session.getTalentCount(skillType), AQUA),
                                    text("tp " + session.getTalentPoints(skillType) + "/" + session.getTotalTalentPoints(skillType), YELLOW)));
        }
        return true;
    }

    private boolean addSkillPoints(CommandSender sender, String[] args) {
        if (args.length != 3) return false;
        PlayerCache target = PlayerCache.require(args[0]);
        SkillType skillType = SkillType.require(args[1]);
        int amount = CommandArgCompleter.requireInt(args[2], i -> i != 0);
        Session session = plugin.sessions.getOrCreateForAdmin(target.uuid);
        session.getSkill(skillType).addSkillPoints(amount);
        sender.sendMessage(text("Gave " + target.name + " " + amount + " " + skillType.displayName + " SP", YELLOW));
        return true;
    }

    private boolean setSkillLevel(CommandSender sender, String[] args) {
        if (args.length != 3) return false;
        PlayerCache target = PlayerCache.require(args[0]);
        SkillType skillType = SkillType.require(args[1]);
        int level = CommandArgCompleter.requireInt(args[2], i -> i >= 0);
        Session session = plugin.sessions.getOrCreateForAdmin(target.uuid);
        session.getSkill(skillType).setSkillLevel(level);
        sender.sendMessage(text("set " + skillType.displayName + " level of " + target.name + " to " + level, YELLOW));
        return true;
    }

    private boolean debugSkill(Player player, String[] args) {
        if (args.length != 1) return false;
        final SkillType skillType = CommandArgCompleter.requireEnum(SkillType.class, args[0]);
        final Session session = plugin.sessions.of(player);
        final boolean result = session.toggleDebugSkill(skillType);
        if (result) {
            player.sendMessage(textOfChildren(text("Debugging "), skillType.getIconTitle(), text(" is now enabled")).color(GREEN));
        } else {
            player.sendMessage(textOfChildren(text("Debugging "), skillType.getIconTitle(), text(" is now disabled")).color(RED));
        }
        return true;
    }

    private boolean debugTalent(Player player, String[] args) {
        if (args.length != 1) return false;
        final TalentType talentType = CommandArgCompleter.requireEnum(TalentType.class, args[0]);
        final Session session = plugin.sessions.of(player);
        final boolean result = session.toggleDebugTalent(talentType);
        if (result) {
            player.sendMessage(textOfChildren(text("Debugging "), talentType, text(" is now enabled")).color(GREEN));
        } else {
            player.sendMessage(textOfChildren(text("Debugging "), talentType, text(" is now disabled")).color(RED));
        }
        return true;
    }
}
