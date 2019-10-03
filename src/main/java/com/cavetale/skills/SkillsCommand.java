package com.cavetale.skills;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
final class SkillsCommand implements TabExecutor {
    private final SkillsPlugin plugin;

    // Error Class

    static class Wrong extends Exception {
        Wrong(final String msg) {
            super(msg);
        }
    }

    // Overrides

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String alias, String[] args) {
        if (args.length == 0) {
            commandHelp(sender);
            return true;
        }
        try {
            String[] argl = Arrays.copyOfRange(args, 1, args.length);
            boolean res = onCommand(sender, args[0], argl);
            if (!res) {
                commandHelp(sender);
            }
            return true;
        } catch (Wrong e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 0) {
            return null;
        }
        if (args.length == 1) {
            final List<String> commands = Arrays.asList("a");
            return complete(args[0], commands);
        }
        if (args.length == 2 && args[0].equals("a")) {
            final List<String> list = Arrays.asList("1", "2");
            return complete(args[1], list);
        }
        return null;
    }

    // Effective Implementations

    private boolean onCommand(CommandSender sender, String cmd,
                              String[] args) throws Wrong {
        switch (cmd) {
        case "talent": return talentCommand(requirePlayer(sender), args);
        case "boss": {
            Player player = requirePlayer(sender);
            if (!player.isOp()) return false;
            Boss boss = new Boss(plugin, Boss.Type.valueOf(args[0].toUpperCase()), 1);
            boss.hero = player.getUniqueId();
            boss.spawn(player.getLocation());
            return true;
        }
        case "reloadadvancements": {
            if (!sender.isOp()) return false;
            sender.sendMessage("Reloading advancements...");
            plugin.unloadAdvancements();
            plugin.loadAdvancements();
            sender.sendMessage("Advancements reloaded.");
            return true;
        }
        case "gimme": {
            Player player = requirePlayer(sender);
            if (!player.isOp()) return false;
            plugin.addTalentPoints(player, 1);
            return true;
        }
        default:
            return false;
        }
    }

    boolean talentCommand(@NonNull Player player, String[] args) throws Wrong {
        if (args.length == 0) {
            talentMenu(player);
            return true;
        }
        switch (args[0]) {
        case "unlock": {
            if (args.length != 2) return false;
            Session session = plugin.sessionOf(player);
            if (session.getTalentPoints() < session.getTalentCost()) {
                throw new Wrong("You don't have enough Talent Points!");
            }
            Talent talent = Talent.of(args[1]);
            if (talent == null) {
                throw new Wrong("Invalid talent!");
            }
            if (session.hasTalent(talent)) {
                throw new Wrong("Already unlocked!");
            }
            if (!session.canAccessTalent(talent)) {
                throw new Wrong("Parent not yet available!");
            }
            if (!plugin.unlockTalent(player, talent)) {
                throw new Wrong("An unknown error occured.");
            }
            Effects.talentUnlock(player);
            talentMenu(player);
            return true;
        }
        default: break;
        }
        return false;
    }

    void talentMenu(@NonNull Player player) {
        player.sendMessage("");
        player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "Skill Talents");
        Session session = plugin.sessionOf(player);
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
            TalentInfo info = plugin.getTalentInfo(talent.key);
            ChatColor talentColor;
            if (session.hasTalent(talent)) {
                cb.append(info.title).color(ChatColor.GREEN);
                talentColor = ChatColor.GREEN;
            } else if (session.canAccessTalent(talent)
                       && session.getTalentPoints() >= session.getTalentCost()) {
                cb.append("[" + info.title + "]").color(ChatColor.GOLD);
                cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/sk talent unlock " + talent.key));
                talentColor = ChatColor.GOLD;
            } else {
                cb.append(info.title).color(ChatColor.GRAY);
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
                    + depColor + plugin.getTalentInfo(talent.depends.key).title;
            }
            cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    TextComponent
                                    .fromLegacyText("" + ChatColor.WHITE + info.title
                                                    + dependency
                                                    + "\n" + talentColor
                                                    + info.description)));
        }
        player.spigot().sendMessage(cb.create());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Talent Points: "
                           + ChatColor.WHITE + session.getTalentPoints());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Next Unlock: "
                           + ChatColor.WHITE + session.getTalentCost());
        player.sendMessage("");
    }

    void commandHelp(final CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Usage: /skills");
    }

    // Helpers

    private List<String> complete(final String arg,
                                  final List<String> opt) {
        return opt.stream().filter(o -> o.startsWith(arg))
            .collect(Collectors.toList());
    }

    private List<String> complete(final String arg,
                                  final Stream<String> opt) {
        return opt.filter(o -> o.startsWith(arg))
            .collect(Collectors.toList());
    }

    // Wrong Throwers

    Player requirePlayer(final CommandSender sender) throws Wrong {
        if (!(sender instanceof Player)) {
            throw new Wrong("Player required");
        }
        return (Player) sender;
    }
}
