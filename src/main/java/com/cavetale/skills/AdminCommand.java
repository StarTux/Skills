package com.cavetale.skills;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class AdminCommand extends AbstractCommand<SkillsPlugin> {
    protected AdminCommand(final SkillsPlugin plugin) {
        super(plugin, "skilladmin");
    }

    @Override
    protected void onEnable() {
        rootNode.addChild("addtalentpoints").arguments("<player> <skillType> <amount>")
            .description("Add Talent Points")
            .completers(CommandArgCompleter.NULL,
                        CommandArgCompleter.enumLowerList(SkillType.class),
                        CommandArgCompleter.integer(i -> i > 0))
            .playerCaller(this::addTalentPoints);
        rootNode.addChild("addskillpoints").arguments("<player> <skillType> <amount>")
            .description("Add Skill Points")
            .completers(CommandArgCompleter.NULL,
                        CommandArgCompleter.enumLowerList(SkillType.class),
                        CommandArgCompleter.integer(i -> i > 0))
            .playerCaller(this::addSkillPoints);
        rootNode.addChild("debug").denyTabCompletion()
            .description("Toggle your debug mode")
            .playerCaller(this::debug);
    }

    protected boolean addTalentPoints(Player sender, String[] args) {
        if (args.length != 3) return false;
        String argTarget = args[0];
        String argSkillType = args[1];
        String argAmount = args[2];
        Player target = Bukkit.getPlayerExact(argTarget);
        if (target == null) {
            throw new CommandWarn("Player not found: " + argTarget);
        }
        final SkillType skillType = SkillType.ofKey(argSkillType);
        if (skillType == null) {
            throw new CommandWarn("Unknown skill: " + argSkillType);
        }
        final int amount;
        try {
            amount = Integer.parseInt(argAmount);
        } catch (IllegalArgumentException iae) {
            throw new CommandWarn("Invalid amount: " + argAmount);
        }
        if (amount < 1) throw new CommandWarn("Invalid amount: " + argAmount);
        plugin.sessions.apply(target, s -> s.getSkill(skillType).modifyTalents(amount, 0, () -> {
                    sender.sendMessage(text(amount + " Talent Points added", AQUA));
                }));
        return true;
    }

    protected boolean addSkillPoints(Player sender, String[] args) {
        if (args.length != 3) return false;
        String argTarget = args[0];
        String argSkillType = args[1];
        String argAmount = args[2];
        Player target = Bukkit.getPlayerExact(argTarget);
        if (target == null) {
            throw new CommandWarn("Player not found: " + argTarget);
        }
        final SkillType skillType = SkillType.ofKey(argSkillType);
        if (skillType == null) {
            throw new CommandWarn("Unknown skill: " + argSkillType);
        }
        final int amount;
        try {
            amount = Integer.parseInt(argAmount);
        } catch (IllegalArgumentException iae) {
            throw new CommandWarn("Invalid amount: " + argAmount);
        }
        if (amount < 1) throw new CommandWarn("Invalid amount: " + argAmount);
        plugin.sessions.apply(target, s -> s.getSkill(skillType).addSkillPoints(amount));
        sender.sendMessage(text(amount + " Skill Points added", AQUA));
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
