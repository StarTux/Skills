package com.cavetale.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
final class Advancements {
    final SkillsPlugin plugin;

    void unloadAll() {
        List<NamespacedKey> keys = new ArrayList<>();
        Iterator<Advancement> iter = plugin.getServer().advancementIterator();
        while (iter.hasNext()) {
            Advancement it = iter.next();
            NamespacedKey key = it.getKey();
            if (key.getNamespace().equals("skills")) keys.add(key);
        }
        for (NamespacedKey key : keys) {
            plugin.getServer().getUnsafe().removeAdvancement(key);
        }
        plugin.getServer().reloadData();
    }

    void loadAll() {
        load(null);
        for (Talent talent : Talent.values()) {
            load(talent);
        }
        plugin.getServer().reloadData();
    }

    void load(Talent talent) {
        String name = talent != null ? talent.key : "talents";
        NamespacedKey key = new NamespacedKey(plugin, "talents/" + name);
        if (plugin.getServer().getAdvancement(key) != null) return;
        try {
            plugin.getServer().getUnsafe().loadAdvancement(key, make(talent));
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
        plugin.getLogger().info("Talent advancement loaded: " + name);
    }

    String make(Talent talent) {
        String name;
        String parent;
        if (talent != null) {
            name = talent.key;
            parent = talent.depends == null
                ? "skills:talents/talents"
                : "skills:talents/" + talent.depends.key;
        } else {
            name = "talents";
            parent = null;
        }
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> display = new HashMap<>();
        map.put("display", display);
        Map<String, Object> iconMap = new HashMap<>();
        display.put("icon", iconMap);
        TalentInfo info = plugin.talents.getInfo(name);
        iconMap.put("item", "minecraft:" + info.icon);
        if (info.iconNBT != null) iconMap.put("nbt", info.iconNBT);
        display.put("title", info.title);
        display.put("description", info.description);
        if (parent == null && info.background != null) {
            display.put("background", info.background);
        }
        display.put("hidden", false);
        display.put("announce_to_chat", true);
        display.put("show_toast", true);
        if (talent != null) {
            if (talent.terminal) {
                display.put("frame", "goal");
            }
        } else {
            display.put("frame", "challenge");
        }
        Map<String, Object> criteriaMap = new HashMap<>();
        map.put("criteria", criteriaMap);
        map.put("parent", parent);
        Map<String, Object> impossibleMap = new HashMap<>();
        criteriaMap.put("impossible", impossibleMap);
        impossibleMap.put("trigger", "minecraft:impossible");
        return plugin.json.pretty(map);
    }
    /**
     * Unlock the advancement belonging to the given talent.
     * @param player The player
     * @param talent The talent, or null for the root advancement.
     * @return true if advancements were changed, false otherwise.
     */
    boolean give(@NonNull Player player, Talent talent) {
        // talent == null => root advancement ("talents/talents")
        String name = talent != null ? talent.key : "talents";
        NamespacedKey key = new NamespacedKey(plugin, "talents/" + name);
        Advancement advancement = plugin.getServer().getAdvancement(key);
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (progress.isDone()) return false;
        progress.awardCriteria("impossible");
        return true;
    }

    boolean revoke(@NonNull Player player, Talent talent) {
        // talent == null => root advancement ("talents/talents")
        String name = talent != null ? talent.key : "talents";
        NamespacedKey key = new NamespacedKey(plugin, "talents/" + name);
        Advancement advancement = plugin.getServer().getAdvancement(key);
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (!progress.isDone()) return false;
        progress.revokeCriteria("impossible");
        return true;
    }
}
