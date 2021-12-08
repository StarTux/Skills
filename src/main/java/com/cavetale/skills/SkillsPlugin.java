package com.cavetale.skills;

import com.cavetale.core.util.Json;
import com.cavetale.skills.session.Sessions;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.worldmarker.WorldMarkerManager;
import com.winthier.sql.SQLDatabase;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkillsPlugin extends JavaPlugin {
    @Getter protected static SkillsPlugin instance;
    protected SkillsCommand skillsCommand = new SkillsCommand(this);
    protected AdminCommand adminCommand = new AdminCommand(this);
    protected EventListener eventListener = new EventListener(this);
    protected Farming farming = new Farming(this);
    public final Random random = ThreadLocalRandom.current();
    public final SQLDatabase database = new SQLDatabase(this);
    public final Sessions sessions = new Sessions(this);
    protected final Mining mining = new Mining(this);
    protected final Combat combat = new Combat(this);
    protected final Map<String, TalentInfo> talentInfos = new HashMap<>();
    protected final Map<String, Info> infos = new HashMap<>();
    @Getter private final WorldMarkerManager worldMarkerManager = new WorldMarkerManager(this);

    @Override
    public void onEnable() {
        instance = this;
        skillsCommand.enable();
        adminCommand.enable();
        getServer().getPluginManager().registerEvents(eventListener, this);
        database.registerTables(SQLSkill.class, SQLPlayer.class);
        database.createAllTables();
        loadAdvancements();
        sessions.enable();
        worldMarkerManager.enable();
        loadInfos();
    }

    @Override
    public void onDisable() {
        sessions.disable();
        worldMarkerManager.disable();
    }

    public static int pointsForLevelUp(final int lvl) {
        return lvl * 50 + lvl * lvl * 10;
    }

    public static int expBonusForLevel(final int lvl) {
        return lvl;
    }

    /**
     * Unlock the advancement belonging to the given talent.
     * @param player The player
     * @param talent The talent, or null for the root advancement.
     * @return true if advancements were changed, false otherwise.
     */
    public boolean giveAdvancement(@NonNull Player player, Talent talent) {
        // talent == null => root advancement ("talents/talents")
        String name = talent != null ? talent.key : "talents";
        NamespacedKey key = new NamespacedKey(this, "talents/" + name);
        Advancement advancement = getServer().getAdvancement(key);
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (progress.isDone()) return false;
        progress.awardCriteria("impossible");
        return true;
    }

    public boolean revokeAdvancement(@NonNull Player player, Talent talent) {
        // talent == null => root advancement ("talents/talents")
        String name = talent != null ? talent.key : "talents";
        NamespacedKey key = new NamespacedKey(this, "talents/" + name);
        Advancement advancement = getServer().getAdvancement(key);
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (!progress.isDone()) return false;
        progress.revokeCriteria("impossible");
        return true;
    }

    public void saveSQL(@NonNull Object row) {
        if (isEnabled()) {
            database.saveAsync(row, null);
        } else {
            database.save(row);
        }
    }

    protected ConfigurationSection loadYamlResource(@NonNull final String name) {
        return YamlConfiguration.loadConfiguration(new InputStreamReader(getResource(name)));
    }

    protected void loadInfos() {
        ConfigurationSection conf;
        infos.clear();
        conf = loadYamlResource("infos.yml");
        for (String key : conf.getKeys(false)) {
            infos.put(key, new Info(conf.getConfigurationSection(key)));
        }
        talentInfos.clear();
        conf = loadYamlResource("talents.yml");
        for (String key : conf.getKeys(false)) {
            talentInfos.put(key, new TalentInfo(conf.getConfigurationSection(key)));
        }
    }

    // Never returns null
    protected TalentInfo getTalentInfo(String name) {
        TalentInfo result = talentInfos.get(name);
        if (result == null) {
            getLogger().warning("Missing talent info: " + name);
            result = new TalentInfo(name);
            talentInfos.put(name, result);
        }
        return result;
    }

    @SuppressWarnings("deprecation") // getUnsafe
    protected void unloadAdvancements() {
        List<NamespacedKey> keys = new ArrayList<>();
        for (Iterator<Advancement> iter = getServer().advancementIterator(); iter.hasNext();) {
            Advancement it = iter.next();
            NamespacedKey key = it.getKey();
            if (key.getNamespace().equals("skills")) keys.add(key);
        }
        for (NamespacedKey key : keys) {
            getServer().getUnsafe().removeAdvancement(key);
        }
        getServer().reloadData();
    }

    protected void loadAdvancements() {
        loadAdvancement(null);
        for (Talent talent : Talent.values()) {
            loadAdvancement(talent);
        }
        getServer().reloadData();
    }

    @SuppressWarnings("deprecation") // getUnsafe
    protected void loadAdvancement(Talent talent) {
        String name = talent != null ? talent.key : "talents";
        NamespacedKey key = new NamespacedKey(this, "talents/" + name);
        if (getServer().getAdvancement(key) != null) return;
        try {
            getServer().getUnsafe().loadAdvancement(key, makeAdvancement(talent));
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
        getLogger().info("Talent advancement loaded: " + name);
    }

    protected String makeAdvancement(Talent talent) {
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
        TalentInfo info = getTalentInfo(name);
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
