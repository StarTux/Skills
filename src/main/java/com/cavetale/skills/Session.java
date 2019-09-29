package com.cavetale.skills;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.entity.Player;

final class Session {
    final SkillsPlugin plugin;
    final UUID uuid;
    SQLPlayer playerColumn;
    EnumMap<SkillType, SQLSkill> skillColumns = new EnumMap<>(SkillType.class);

    Session(@NonNull final SkillsPlugin plugin,
            @NonNull final Player player,
            @NonNull final SQLPlayer playerColumn,
            @NonNull final Map<SkillType, SQLSkill> inSkillColumns) {
        this.plugin = plugin;
        this.uuid = player.getUniqueId();
        this.playerColumn = playerColumn;
        this.skillColumns.putAll(inSkillColumns);
    }

    void saveData() {
        plugin.database.saveAsync(playerColumn, null);
        for (SQLSkill col : skillColumns.values()) {
            if (!col.modified) continue;
            col.modified = false;
            plugin.database.saveAsync(col, null);
        }
    }
}
