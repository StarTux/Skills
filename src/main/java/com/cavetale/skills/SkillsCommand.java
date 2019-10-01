package com.cavetale.skills;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
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
        case "a": {
            sender.sendMessage("a");
            return true;
        }
        case "boss": {
            Player player = (Player) sender;
            Boss boss = new Boss(plugin, Boss.Type.valueOf(args[0].toUpperCase()), 1);
            boss.hero = player.getUniqueId();
            boss.spawn(player.getLocation());
            return true;
        }
        default:
            return false;
        }
    }

    void commandHelp(final CommandSender sender) {
        sender.sendMessage("Usage: /skills");
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

    Player expectPlayer(final CommandSender sender) throws Wrong {
        if (!(sender instanceof Player)) {
            throw new Wrong("Player expected");
        }
        return (Player) sender;
    }
}
