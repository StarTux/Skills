package com.cavetale.skills;

import com.winthier.sql.SQLDatabase;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkillsPlugin extends JavaPlugin {
    SkillsCommand skillsCommand = new SkillsCommand(this);
    EventListener eventListener = new EventListener(this);
    Growstick growstick = new Growstick(this);
    Random random = ThreadLocalRandom.current();
    SQLDatabase database = new SQLDatabase(this);
    final List<SQLSkill> skillColumns = new ArrayList<>();
    final Map<UUID, SQLPlayer> playerColumns = new HashMap<>();
    final Map<UUID, Session> sessions = new HashMap<>();
    final Mining mining = new Mining(this);
    final Combat combat = new Combat(this);
    final Metadata meta = new Metadata(this);

    @Override
    public void onEnable() {
        getCommand("skills").setExecutor(skillsCommand);
        getServer().getPluginManager().registerEvents(eventListener, this);
        database.registerTables(SQLSkill.class, SQLPlayer.class);
        database.createAllTables();
        loadDatabase();
        for (Player player : getServer().getOnlinePlayers()) {
            loadSession(player);
        }
        getServer().getScheduler().runTaskTimer(this, this::onTick, 1, 1);
    }

    @Override
    public void onDisable() {
        for (Session session : sessions.values()) {
            session.onDisable();
            session.saveData();
        }
        sessions.clear();
    }

    void onTick() {
        for (Session session : sessions.values()) {
            session.onTick();
        }
    }

    void loadDatabase() {
        skillColumns.clear();
        skillColumns.addAll(database.find(SQLSkill.class).findList());
        List<SQLPlayer> players = database.find(SQLPlayer.class).findList();
        playerColumns.clear();
        for (SQLPlayer player : players) {
            playerColumns.put(player.uuid, player);
        }
    }

    SQLPlayer playerColumnOf(@NonNull UUID uuid) {
        SQLPlayer result = playerColumns.get(uuid);
        if (result == null) {
            result = new SQLPlayer(uuid);
            database.saveAsync(result, null);
            playerColumns.put(uuid, result);
        }
        return result;
    }

    Map<SkillType, SQLSkill> skillColumnsOf(@NonNull UUID uuid) {
        Map<SkillType, SQLSkill> map = new EnumMap<>(SkillType.class);
        for (SQLSkill col : skillColumns) {
            if (!uuid.equals(col.player)) continue;
            SkillType skill = col.getSkillType();
            if (skill == null) continue;
            map.put(skill, col);
        }
        for (SkillType skill : SkillType.values()) {
            if (map.containsKey(skill)) continue;
            SQLSkill col = new SQLSkill(uuid, skill.key);
            database.saveAsync(col, null);
            map.put(skill, col);
        }
        return map;
    }

    Session sessionOf(@NonNull Player player) {
        final UUID uuid = player.getUniqueId();
        Session session = sessions.get(uuid);
        if (session == null) {
            session = new Session(this, player,
                                  playerColumnOf(uuid),
                                  skillColumnsOf(uuid));
            sessions.put(session.uuid, session);
        }
        return session;
    }

    void loadSession(@NonNull Player player) {
        sessionOf(player);
    }

    void removeSession(@NonNull Player player) {
        Session session = sessions.remove(player.getUniqueId());
        if (session != null) {
            session.onDisable();
            session.saveData();
        }
    }

    int pointsForLevelUp(final int lvl) {
        return lvl * 50 + lvl * lvl * 10;
    }

    void addSkillPoints(@NonNull Player player, @NonNull SkillType skill, final int add) {
        Session session = sessionOf(player);
        SQLSkill col = session.skillColumns.get(skill);
        int points = col.points + add;
        int req = pointsForLevelUp(col.level + 1);
        if (points >= req) {
            points -= req;
            col.level += 1;
            // TODO effect
        }
        col.points = points;
        col.modified = true;
        sessionOf(player).showSkillBar(skill, col.level, points, req);
        player.sendActionBar(ChatColor.GRAY + "+"
                             + ChatColor.GOLD + ChatColor.BOLD + add
                             + ChatColor.GRAY + "SP");
    }
}
