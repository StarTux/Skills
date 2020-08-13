package com.cavetale.skills;

import com.cavetale.skills.command.CommandWarn;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Superclass of commands, providing utilities.
 */
abstract class CommandBase {
    protected Player playerOf(String arg) {
        Player player = Bukkit.getPlayer(arg);
        if (player == null) throw new CommandWarn("Player not found: " + arg);
        return player;
    }

    protected int parseInt(String arg) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException nfe) {
            throw new CommandWarn("Number expected: " + arg);
        }
    }
}
