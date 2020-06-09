package com.cavetale.skills;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Superclass of commands, providing utilities.
 */
abstract class CommandBase {
    protected static final class Wrong extends Exception {
        Wrong(final String msg) {
            super(msg);
        }
    }

    protected List<String> complete(String arg, Collection<String> opt) {
        return opt.stream().filter(o -> o.startsWith(arg))
            .collect(Collectors.toList());
    }

    protected List<String> complete(String arg, Stream<String> opt) {
        return opt.filter(o -> o.startsWith(arg))
            .collect(Collectors.toList());
    }

    protected Player requirePlayer(CommandSender sender) throws Wrong {
        if (!(sender instanceof Player)) {
            throw new Wrong("Player required");
        }
        return (Player) sender;
    }

    protected Player findPlayer(String arg) throws Wrong {
        Player player = Bukkit.getPlayer(arg);
        if (player == null) throw new Wrong("Player not found: " + arg);
        return player;
    }

    protected int parseInt(String arg) throws Wrong {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException nfe) {
            throw new Wrong("Number expected: " + arg);
        }
    }
}
