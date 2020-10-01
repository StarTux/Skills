package com.cavetale.skills;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class Sessions {
    final SkillsPlugin plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();

    public void disable() {
        for (Session session : sessions.values()) {
            session.onDisable();
            session.saveData();
        }
        sessions.clear();
    }

    public void tick(int ticks) {
        for (Session session : sessions.values()) {
            session.tick(ticks);
        }
    }

    public Session of(@NonNull Player player) {
        final UUID uuid = player.getUniqueId();
        Session session = sessions.computeIfAbsent(uuid, u -> new Session(plugin, u));
        return session;
    }

    /**
     * Load a session and ensure that its state and server
     * advancements are consistent.
     */
    public void load(@NonNull Player player) {
        Session session = of(player);
        if (session.talents.isEmpty() && session.getTalentPoints() == 0) {
            plugin.advancements.revoke(player, Talent.ROOT);
        } else {
            plugin.advancements.give(player, Talent.ROOT);
        }
        for (Talent talent : Talent.values()) {
            if (session.hasTalent(talent)) {
                plugin.advancements.give(player, talent);
            } else {
                plugin.advancements.revoke(player, talent);
            }
        }
    }

    public void remove(@NonNull Player player) {
        Session session = sessions.remove(player.getUniqueId());
        if (session != null) {
            session.onDisable();
            session.saveData();
        }
    }
}
