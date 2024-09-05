package com.cavetale.skills.session;

import com.cavetale.core.event.hud.PlayerHudEvent;
import com.cavetale.skills.talent.TalentType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

@Getter
public final class Sessions implements Listener {
    protected final Map<UUID, Session> sessionsMap = new HashMap<>();

    public void enable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            createAsync(player);
        }
        Bukkit.getPluginManager().registerEvents(this, skillsPlugin());
    }

    public void disable() {
        for (Session session : sessionsMap.values()) {
            session.disable();
        }
        sessionsMap.clear();
    }

    public void reloadAll() {
        disable();
        enable();
    }

    /**
     * Load and prepare for live use.
     */
    private Session createAsync(Player player) {
        remove(player);
        Session session = new Session(player);
        sessionsMap.put(session.uuid, session);
        session.loadAsync(() -> session.enable());
        return session;
    }

    private void remove(@NonNull Player player) {
        Session session = sessionsMap.remove(player.getUniqueId());
        if (session == null) return;
        session.disable();
    }

    private void reload(Player player) {
        remove(player);
        createAsync(player);
    }

    /**
     * Get the session if it exists.  If not, create it synchronously.
     * Never yields null, but may return a session which is not
     * enabled yet, thus some of its methods will have undefined
     * behavior.
     */
    public Session of(Player player) {
        Session session = sessionsMap.get(player.getUniqueId());
        return session != null ? session : createAsync(player);
    }

    /**
     * Get session and apply callback if session is loaded and enabled.
     *
     * @param player the player
     * @param callback the callback
     *
     * @return true if callback was applied, thus session is loaded
     *   and enabled, False otherwise.
     */
    public boolean apply(Player player, Consumer<Session> callback) {
        Session session = sessionsMap.get(player.getUniqueId());
        if (session == null || !session.isEnabled()) return false;
        callback.accept(session);
        return true;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onPlayerJoin(PlayerJoinEvent event) {
        createAsync(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerQuit(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler
    private void onPlayerHud(PlayerHudEvent event) {
        apply(event.getPlayer(), session -> session.onPlayerHud(event));
    }

    public boolean isTalentEnabled(Player player, TalentType talentType) {
        Session session = of(player);
        return session.isEnabled() ? session.isTalentEnabled(talentType) : false;
    }

    public Session getOrCreateForAdmin(UUID uuid) {
        Session session = sessionsMap.get(uuid);
        if (session != null && session.isEnabled()) return session;
        session = new Session(uuid);
        session.loadAll();
        return session;
    }
}
