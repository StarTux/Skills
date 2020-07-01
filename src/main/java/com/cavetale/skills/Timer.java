package com.cavetale.skills;

import com.cavetale.worldmarker.BlockMarker;
import com.cavetale.worldmarker.MarkBlock;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Timer object to run stuff every tick.
 * Owned by SkillsPlugin.
 */
@RequiredArgsConstructor
public final class Timer {
    private final SkillsPlugin plugin;
    private long ticks = 0;

    void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::onTick, 1, 1);
    }

    void onTick() {
        ticks += 1;
        plugin.sessions.tick();
        Gui.onTick(plugin);
    }
}
