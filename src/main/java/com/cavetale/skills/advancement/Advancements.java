package com.cavetale.skills.advancement;

import com.cavetale.core.util.Json;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.TalentInfo;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        NamespacedKey key = new NamespacedKey(plugin, talent.skillType.key + "/" + talent.key);
        return give(player, key);
    }

    public boolean revoke(@NonNull Player player, TalentType talent) {
        NamespacedKey key = new NamespacedKey(plugin, talent.skillType.key + "/" + talent.key);
        return revoke(player, key);
    }

    public boolean give(@NonNull Player player, SkillType skillType) {
        NamespacedKey key = new NamespacedKey(plugin, skillType.key + "/" + skillType.key);
        return give(player, key);
    }

    public boolean revoke(@NonNull Player player, SkillType skillType) {
        NamespacedKey key = new NamespacedKey(plugin, skillType.key + "/" + skillType.key);
        return revoke(player, key);
    }

    private boolean give(Player player, NamespacedKey key) {
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement == null) return false;
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (progress.isDone()) return false;
        progress.awardCriteria("impossible");
        return true;
    }

    private boolean revoke(Player player, NamespacedKey key) {
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement == null) return false;
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (!progress.isDone()) return false;
        progress.revokeCriteria("impossible");
        return true;
    }

    @SuppressWarnings("deprecation") // getUnsafe
    public void removeAll() {
        List<NamespacedKey> removeKeys = new ArrayList<>();
        for (Iterator<Advancement> iter = Bukkit.advancementIterator(); iter.hasNext();) {
            Advancement it = iter.next();
            NamespacedKey key = it.getKey();
            if (key.getNamespace().equals("skills")) removeKeys.add(key);
        }
        for (NamespacedKey key : removeKeys) {
            Bukkit.getUnsafe().removeAdvancement(key);
        }
        Bukkit.reloadData();
    }

    public void createAll() {
        for (SkillType skillType : SkillType.values()) {
            create(skillType);
        }
        for (TalentType talent : TalentType.values()) {
            create(talent);
        }
        Bukkit.reloadData();
    }

    @SuppressWarnings("deprecation") // getUnsafe
    protected void create(TalentType talent) {
        String name = talent.key;
        NamespacedKey key = new NamespacedKey(plugin, talent.skillType.key + "/" + talent.key);
        if (Bukkit.getAdvancement(key) != null) return;
        AdvancementJson advancement = make(talent);
        try {
            Bukkit.getUnsafe().loadAdvancement(key, Json.serialize(make(talent)));
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation") // getUnsafe
    protected void create(SkillType skillType) {
        NamespacedKey key = new NamespacedKey(plugin, skillType.key + "/" + skillType.key);
        if (Bukkit.getAdvancement(key) != null) return;
        AdvancementJson advancement = make(skillType);
        try {
            Bukkit.getUnsafe().loadAdvancement(key, Json.serialize(advancement));
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
    }

    protected AdvancementJson make(TalentType talentType) {
        TalentInfo info = plugin.getTalentInfo(talentType.key);
        String parent = talentType.depends == null
            ? "skills:" + talentType.skillType.key + "/" + talentType.skillType.key
            : "skills:" + talentType.skillType.key + "/" + talentType.depends.key;
        AdvancementJson advancement = new AdvancementJson();
        advancement.display.icon.item = "minecraft:" + info.getIcon();
        if (info.getIconNBT() != null) {
            advancement.display.icon.nbt = info.getIconNBT();
        }
        advancement.display.title = info.getTitle();
        advancement.display.description = info.getDescription();
        advancement.display.hidden = false;
        advancement.display.announce_to_chat = false;
        advancement.display.show_toast = true;
        advancement.display.frame = talentType.isTerminal() ? "goal" : null;
        advancement.parent = parent;
        return advancement;
    }

    protected AdvancementJson make(SkillType skillType) {
        TalentInfo info = plugin.getTalentInfo(skillType.key);
        AdvancementJson advancement = new AdvancementJson();
        advancement.display.icon.item = "minecraft:" + info.getIcon();
        if (info.getIconNBT() != null) {
            advancement.display.icon.nbt = info.getIconNBT();
        }
        advancement.display.title = info.getTitle();
        advancement.display.description = info.getDescription();
        if (info.getBackground() != null) {
            advancement.display.background = info.getBackground();
        }
        advancement.display.hidden = false;
        advancement.display.announce_to_chat = false;
        advancement.display.show_toast = true;
        advancement.display.frame = "challenge";
        advancement.parent = null;
        return advancement;
    }
}
