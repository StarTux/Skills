package com.cavetale.skills.util;

import java.util.function.Consumer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Utility class to affect a player as well as their spectators, if
 * any.
 */
public final class Spectators {
    private Spectators() { }

    public static void apply(Player player, Consumer<Player> func) {
        func.accept(player);
        for (Player spec : player.getWorld().getPlayers()) {
            if (spec.getGameMode() != GameMode.SPECTATOR) continue;
            if (spec.equals(player)) continue;
            if (player.equals(spec.getSpectatorTarget())) {
                func.accept(spec);
            }
        }
    }
}
