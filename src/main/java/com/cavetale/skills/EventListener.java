package com.cavetale.skills;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
final class EventListener implements Listener {
    final SkillsPlugin plugin;

    @EventHandler(priority = EventPriority.LOW)
    void onPlayerJoin(PlayerJoinEvent event) {
        plugin.sessions.load(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerQuit(PlayerQuitEvent event) {
        plugin.sessions.remove(event.getPlayer());
    }
}
