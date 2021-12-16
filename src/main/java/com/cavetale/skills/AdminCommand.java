package com.cavetale.skills;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        rootNode.addChild("particles").denyTabCompletion()
            .description("Toggle Particles")
            .playerCaller(this::particles);
        CommandNode advancementNode = rootNode.addChild("advancement")
            .description("Advancement commands");
        advancementNode.addChild("reload").denyTabCompletion()
            .description("Reload Advancements")
            .senderCaller(this::advancementReload);
        advancementNode.addChild("create").denyTabCompletion()
            .description("Create Advancements")
            .senderCaller(this::advancementCreate);
        advancementNode.addChild("remove").denyTabCompletion()
            .description("Remove Advancements")
            .senderCaller(this::advancementRemove);
        advancementNode.addChild("reload").denyTabCompletion()
            .description("Reload Advancements")
            .senderCaller(this::advancementReload);
        advancementNode.addChild("update").denyTabCompletion()
            .description("Update all player advancements")
            .senderCaller(this::advancementUpdate);
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
                    sender.sendMessage(amount + " Talent Points added");
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
        sender.sendMessage(amount + " Skill Points added");
        return true;
    }

    protected boolean particles(Player player, String[] args) {
        if (args.length != 0) return false;
        plugin.sessions.apply(player, session -> {
                session.setNoParticles(!session.isNoParticles());
                player.sendMessage("Particles: " + (session.isNoParticles() ? "off" : "on"));
            });
        return true;
    }

    protected boolean advancementCreate(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        sender.sendMessage("Creating advancements...");
        plugin.advancements.createAll();
        return true;
    }

    protected boolean advancementRemove(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        sender.sendMessage("Removing advancements...");
        plugin.advancements.removeAll();
        return true;
    }

    protected boolean advancementReload(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        sender.sendMessage("Reloading advancements...");
        plugin.advancements.removeAll();
        plugin.advancements.createAll();
        return true;
    }

    protected boolean advancementUpdate(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            Session session = plugin.sessions.of(player);
            if (!session.isEnabled()) continue;
            session.updateAdvancements();
            count += 1;
        }
        sender.sendMessage("Updated advancements of " + count + " player(s)");
        return true;
    }
}
