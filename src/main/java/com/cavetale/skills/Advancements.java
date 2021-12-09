package com.cavetale.skills;

import com.cavetale.core.util.Json;
import com.cavetale.skills.skill.TalentType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class Advancements {
    protected final SkillsPlugin plugin;

    /**
     * Unlock the advancement belonging to the given talent.
     * @param player The player
     * @param talent The talent, or null for the root advancement.
     * @return true if advancements were changed, false otherwise.
     */
    public boolean give(@NonNull Player player, TalentType talent) {
        // talent == null => root advancement ("talents/talents")
        String name = talent != null ? talent.key : "talents";
        NamespacedKey key = new NamespacedKey(plugin, "talents/" + name);
        Advancement advancement = Bukkit.getAdvancement(key);
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (progress.isDone()) return false;
        progress.awardCriteria("impossible");
        return true;
    }

    public boolean revoke(@NonNull Player player, TalentType talent) {
        // talent == null => root advancement ("talents/talents")
        String name = talent != null ? talent.key : "talents";
        NamespacedKey key = new NamespacedKey(plugin, "talents/" + name);
        Advancement advancement = Bukkit.getAdvancement(key);
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (!progress.isDone()) return false;
        progress.revokeCriteria("impossible");
        return true;
    }

    @SuppressWarnings("deprecation") // getUnsafe
    protected void removeAll() {
        List<NamespacedKey> keys = new ArrayList<>();
        for (Iterator<Advancement> iter = Bukkit.advancementIterator(); iter.hasNext();) {
            Advancement it = iter.next();
            NamespacedKey key = it.getKey();
            if (key.getNamespace().equals("skills")) keys.add(key);
        }
        for (NamespacedKey key : keys) {
            Bukkit.getUnsafe().removeAdvancement(key);
        }
        Bukkit.reloadData();
    }

    protected void createAll() {
        create(null);
        for (TalentType talent : TalentType.values()) {
            create(talent);
        }
        Bukkit.reloadData();
    }

    @SuppressWarnings("deprecation") // getUnsafe
    protected void create(TalentType talent) {
        String name = talent != null ? talent.key : "talents";
        NamespacedKey key = new NamespacedKey(plugin, "talents/" + name);
        if (Bukkit.getAdvancement(key) != null) return;
        try {
            Bukkit.getUnsafe().loadAdvancement(key, make(talent));
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
    }

    protected String make(TalentType talent) {
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
        TalentInfo info = plugin.getTalentInfo(name);
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
            if (talent.isTerminal()) {
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
        return Json.prettyPrint(map);
    }
}
