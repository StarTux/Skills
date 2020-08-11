package com.cavetale.skills;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

/**
 * Timer object to run stuff every tick.
 * Owned by SkillsPlugin.
 */
@RequiredArgsConstructor
public final class Timer {
    private final SkillsPlugin plugin;
    private int ticks = 0;

    void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::onTick, 1, 1);
    }

    void onTick() {
        ticks += 1;
        plugin.sessions.tick(ticks);
        Gui.onTick(plugin);
    }
}
