package com.cavetale.skills;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandNode;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import com.winthier.playercache.PlayerCache;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
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
        rootNode.addChild("debug").denyTabCompletion()
            .description("Toggle your debug mode")
            .playerCaller(this::debug);
    }

    private boolean talentInfo(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        PlayerCache target = PlayerCache.require(args[0]);
        final SkillType skillType = SkillType.require(args[1]);
        Session session = plugin.sessions.getOrCreateForAdmin(target.uuid);
        sender.sendMessage(text(target.name + " " + skillType.displayName + " Talent Info", AQUA));
        sender.sendMessage(join(noSeparators(), text("Talent Points ", GRAY),
                                text(session.getTalentPoints(skillType)),
                                text("/", GRAY),
                                text(session.getTotalTalentPoints(skillType))));
        sender.sendMessage(join(noSeparators(), text("Money Bonus ", GRAY),
                                text(session.getMoneyBonus(skillType))));
        sender.sendMessage(join(noSeparators(), text("Exp Bonus ", GRAY),
                                text(session.getExpBonus(skillType))));
        List<Component> components = new ArrayList<>();
        for (TalentType talentType : TalentType.SKILL_MAP.get(skillType)) {
            if (session.isTalentEnabled(talentType)) {
                components.add(text(talentType.key, GREEN));
            } else if (session.hasTalent(talentType)) {
                components.add(text(talentType.key, RED, STRIKETHROUGH));
            }
        }
        sender.sendMessage(join(noSeparators(), text("Talents (" + components.size() + ") ", GRAY),
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

    private void debug(Player player) {
        Session session = plugin.sessions.of(player);
        if (session.isDebugMode()) {
            session.setDebugMode(false);
            player.sendMessage(text("Debug mode disabled", YELLOW));
        } else {
            session.setDebugMode(true);
            player.sendMessage(text("Debug mode enabled", AQUA));
        }
    }
}
