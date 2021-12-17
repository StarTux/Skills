package com.cavetale.skills.util;

import org.bukkit.entity.Player;

public final class Players {
    private Players() { }

    public static boolean playMode(Player player) {
        if (player == null) return false;
        switch (player.getGameMode()) {
        case SURVIVAL:
        case ADVENTURE:
            return player.hasPermission("skills.skills");
        default: return false;
        }
    }
}
