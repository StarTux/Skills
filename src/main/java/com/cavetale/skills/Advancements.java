package com.cavetale.skills;

import com.cavetale.skills.util.Json;
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
public final class Advancements {
    final SkillsPlugin plugin;

    public void unloadAll() {
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

    public void loadAll() {
        for (Talent talent : Talent.values()) {
            load(talent);
        }
        plugin.getServer().reloadData();
    }

    public void load(Talent talent) {
        NamespacedKey key = new NamespacedKey(plugin, "talents/" + talent.key);
        if (plugin.getServer().getAdvancement(key) != null) return;
        try {
            plugin.getServer().getUnsafe().loadAdvancement(key, make(talent));
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
        plugin.getLogger().info("Talent advancement loaded: " + talent.key);
    }

    public String make(Talent talent) {
        String parent;
        if (talent == Talent.ROOT) {
            parent = null;
        } else {
            parent = talent.depends == null
                ? "skills:talents/root"
                : "skills:talents/" + talent.depends.key;
        }
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> display = new HashMap<>();
        map.put("display", display);
        Map<String, Object> iconMap = new HashMap<>();
        display.put("icon", iconMap);
        iconMap.put("item", "minecraft:" + talent.material.name().toLowerCase());
        if (talent.iconNBT != null) iconMap.put("nbt", talent.iconNBT);
        display.put("title", talent.displayName);
        display.put("description", talent.description);
        if (talent == Talent.ROOT) {
            display.put("background", "minecraft:textures/block/diamond_ore.png");
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
        return Json.pretty(map);
    }

    /**
     * Unlock the advancement belonging to the given talent.
     * @param player The player
     * @param talent The talent, or null for the root advancement.
     * @return true if advancements were changed, false otherwise.
     */
    public boolean give(@NonNull Player player, Talent talent) {
        // talent == null => root advancement ("talents/talents")
        String name = talent != null ? talent.key : "talents";
        NamespacedKey key = new NamespacedKey(plugin, "talents/" + name);
        Advancement advancement = plugin.getServer().getAdvancement(key);
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (progress.isDone()) return false;
        progress.awardCriteria("impossible");
        return true;
    }

    public boolean revoke(@NonNull Player player, Talent talent) {
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
