package com.cavetale.skills;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandNode;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.sql.SQLSkill;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AdminCommand extends AbstractCommand<SkillsPlugin> {
    protected AdminCommand(final SkillsPlugin plugin) {
        super(plugin, "skilladmin");
    }

    @Override
    protected void onEnable() {
        rootNode.addChild("gimme").denyTabCompletion()
            .description("Receive a TalentType Point")
            .playerCaller(this::gimme);
        rootNode.addChild("particles").denyTabCompletion()
            .description("Toggle Particles")
            .playerCaller(this::particles);
        rootNode.addChild("median").denyTabCompletion()
            .description("Compute Median")
            .senderCaller(this::median);
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

    protected boolean gimme(Player player, String[] args) {
        if (args.length != 0) return false;
        plugin.sessions.apply(player, s -> s.addTalentPoints(1));
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

    protected boolean median(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        for (SkillType skill : SkillType.values()) {
            List<SQLSkill> rows = plugin.database.find(SQLSkill.class).findList().stream()
                .filter(s -> s.getLevel() > 0)
                .filter(s -> skill.key.equals(s.getSkill()))
                .sorted((b, a) -> Integer.compare(a.getTotalPoints(),
                                                  b.getTotalPoints()))
                .collect(Collectors.toList());
            if (rows.isEmpty()) continue;
            int sumSP = 0;
            int sumLevel = 0;
            for (SQLSkill row : rows) {
                sumSP += row.getTotalPoints();
                sumLevel += row.getLevel();
            }
            int avgSP = sumSP / rows.size();
            int avgLevel = sumLevel / rows.size();
            SQLSkill median = rows.get(rows.size() / 2);
            SQLSkill max = rows.get(0);
            sender.sendMessage(skill.displayName
                               + " Sample=" + rows.size()
                               + " Sum=" + sumSP + "," + sumLevel
                               + " Avg=" + avgSP + "," + avgLevel
                               + " Max=" + max.getTotalPoints() + "," + max.getLevel()
                               + " Med=" + median.getTotalPoints() + "," + median.getLevel());
        }
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
