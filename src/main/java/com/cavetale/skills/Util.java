package com.cavetale.skills;

import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    static String niceEnumName(@NonNull String name) {
        return Stream.of(name.split("_"))
            .map(s -> s.substring(0, 1) + s.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    static String niceEnumName(@NonNull Enum enom) {
        return niceEnumName(enom.name());
    }
}
