package com.cavetale.skills;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Player;

final class Util {
    private Util() { }

    static int dst(@NonNull Location a, @NonNull Location b) {
        int d = Math.max(Math.abs(a.getBlockX() - b.getBlockX()),
                         Math.abs(a.getBlockZ() - b.getBlockZ()));
        return Math.max(d, Math.abs(a.getBlockY() - b.getBlockY()));
    }

    static boolean playMode(@NonNull Player player) {
        switch (player.getGameMode()) {
        case SURVIVAL:
        case ADVENTURE:
            return true;
        default:
            return false;
        }
    }
}
