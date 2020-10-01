package com.cavetale.skills;

import com.winthier.sql.SQLDatabase;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SQL {
    private final SkillsPlugin plugin;
    private SQLDatabase database;
    // Caches
    @Getter final List<SQLSkill> skillRows = new ArrayList<>();
    @Getter final Map<UUID, SQLPlayer> playerRows = new HashMap<>();

    public void enable() {
        database = new SQLDatabase(plugin);
        database.registerTables(SQLSkill.class, SQLPlayer.class);
        database.createAllTables();
    }

    public void loadDatabase() {
        skillRows.clear();
        skillRows.addAll(database.find(SQLSkill.class).findList());
        List<SQLPlayer> players = database.find(SQLPlayer.class).findList();
        playerRows.clear();
        for (SQLPlayer player : players) {
            playerRows.put(player.uuid, player);
        }
    }

    public SQLPlayer playerRowOf(@NonNull UUID uuid) {
        SQLPlayer result = playerRows.get(uuid);
        if (result == null) {
            result = new SQLPlayer(uuid);
            database.saveAsync(result, null);
            playerRows.put(uuid, result);
        }
        return result;
    }

    public Map<SkillType, SQLSkill> skillRowsOf(@NonNull UUID uuid) {
        Map<SkillType, SQLSkill> map = new EnumMap<>(SkillType.class);
        for (SQLSkill col : skillRows) {
            if (!uuid.equals(col.player)) continue;
            SkillType skill = col.getSkillType();
            if (skill == null) continue;
            map.put(skill, col);
        }
        for (SkillType skill : SkillType.values()) {
            if (map.containsKey(skill)) continue;
            SQLSkill col = new SQLSkill(uuid, skill.key);
            database.saveAsync(col, null);
            skillRows.add(col);
            map.put(skill, col);
        }
        return map;
    }

    public void save(@NonNull Object row) {
        if (plugin.isEnabled()) {
            database.saveAsync(row, null);
        } else {
            database.save(row);
        }
    }
}
