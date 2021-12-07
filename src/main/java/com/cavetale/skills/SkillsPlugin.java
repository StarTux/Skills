package com.cavetale.skills;

import com.cavetale.skills.worldmarker.WorldMarkerManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.winthier.sql.SQLDatabase;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkillsPlugin extends JavaPlugin {
    protected SkillsCommand skillsCommand = new SkillsCommand(this);
    protected AdminCommand adminCommand = new AdminCommand(this);
    protected EventListener eventListener = new EventListener(this);
    protected Farming farming = new Farming(this);
    protected Random random = ThreadLocalRandom.current();
    protected SQLDatabase database = new SQLDatabase(this);
    protected final List<SQLSkill> skillColumns = new ArrayList<>();
    protected final Map<UUID, SQLPlayer> playerColumns = new HashMap<>();
    protected final Map<UUID, Session> sessions = new HashMap<>();
    protected final Mining mining = new Mining(this);
    protected final Combat combat = new Combat(this);
    protected final Metadata meta = new Metadata(this);
    protected Gson gson = new Gson();
    protected Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
    protected Map<String, TalentInfo> talentInfos;
    protected Map<String, Info> infos;
    protected long ticks = 0;
    @Getter private final WorldMarkerManager worldMarkerManager = new WorldMarkerManager(this);

    @Override
    public void onEnable() {
        skillsCommand.enable();
        adminCommand.enable();
        getServer().getPluginManager().registerEvents(eventListener, this);
        database.registerTables(SQLSkill.class, SQLPlayer.class);
        database.createAllTables();
        loadDatabase();
        loadAdvancements();
        for (Player player : getServer().getOnlinePlayers()) {
            loadSession(player);
        }
        getServer().getScheduler().runTaskTimer(this, this::onTick, 1, 1);
        worldMarkerManager.enable();
    }

    @Override
    public void onDisable() {
        for (Session session : sessions.values()) {
            session.onDisable();
            session.saveData();
        }
        sessions.clear();
        worldMarkerManager.disable();
    }

    protected void onTick() {
        ticks += 1;
        for (Session session : sessions.values()) {
            session.onTick();
        }
    }

    protected void loadDatabase() {
        skillColumns.clear();
        skillColumns.addAll(database.find(SQLSkill.class).findList());
        List<SQLPlayer> players = database.find(SQLPlayer.class).findList();
        playerColumns.clear();
        for (SQLPlayer player : players) {
            playerColumns.put(player.uuid, player);
        }
    }

    protected SQLPlayer playerColumnOf(@NonNull UUID uuid) {
        SQLPlayer result = playerColumns.get(uuid);
        if (result == null) {
            result = new SQLPlayer(uuid);
            database.saveAsync(result, null);
            playerColumns.put(uuid, result);
        }
        return result;
    }

    protected Map<SkillType, SQLSkill> skillColumnsOf(@NonNull UUID uuid) {
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
            skillColumns.add(col);
            map.put(skill, col);
        }
        return map;
    }

    protected Session sessionOf(@NonNull Player player) {
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

    protected void loadSession(@NonNull Player player) {
        Session session = sessionOf(player);
        if (session.talents.isEmpty() && session.getTalentPoints() == 0) {
            revokeAdvancement(player, null);
        } else {
            giveAdvancement(player, null);
        }
        for (Talent talent : Talent.values()) {
            if (session.hasTalent(talent)) {
                giveAdvancement(player, talent);
            } else {
                revokeAdvancement(player, talent);
            }
        }
    }

    protected void removeSession(@NonNull Player player) {
        Session session = sessions.remove(player.getUniqueId());
        if (session != null) {
            session.onDisable();
            session.saveData();
        }
    }

    static int pointsForLevelUp(final int lvl) {
        return lvl * 50 + lvl * lvl * 10;
    }

    protected void addSkillPoints(@NonNull Player player, @NonNull SkillType skill, final int add) {
        Session session = sessionOf(player);
        SQLSkill col = session.skillColumns.get(skill);
        int points = col.points + add;
        int req = pointsForLevelUp(col.level + 1);
        if (points >= req) {
            points -= req;
            col.level += 1;
            session.playerColumn.levels += 1;
            session.playerColumn.modified = true;
            Effects.levelup(player);
            player.showTitle(Title.title(Component.text(skill.displayName, NamedTextColor.GOLD),
                                         Component.text("Level " + col.level, NamedTextColor.WHITE)));
        }
        col.points = points;
        col.modified = true;
        sessionOf(player).showSkillBar(player, skill, col.level, points, req, add);
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
    protected boolean giveAdvancement(@NonNull Player player, Talent talent) {
        // talent == null => root advancement ("talents/talents")
        String name = talent != null ? talent.key : "talents";
        NamespacedKey key = new NamespacedKey(this, "talents/" + name);
        Advancement advancement = getServer().getAdvancement(key);
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (progress.isDone()) return false;
        progress.awardCriteria("impossible");
        return true;
    }

    protected boolean revokeAdvancement(@NonNull Player player, Talent talent) {
        // talent == null => root advancement ("talents/talents")
        String name = talent != null ? talent.key : "talents";
        NamespacedKey key = new NamespacedKey(this, "talents/" + name);
        Advancement advancement = getServer().getAdvancement(key);
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (!progress.isDone()) return false;
        progress.revokeCriteria("impossible");
        return true;
    }

    protected boolean rollTalentPoint(@NonNull Player player, int increase) {
        final int total = 800;
        Session session = sessionOf(player);
        if (session.talents.size() >= Talent.COUNT) return false;
        session.playerColumn.talentChance += increase;
        session.playerColumn.modified = true;
        int chance;
        if (session.talents.isEmpty() && session.getTalentPoints() == 0) {
            chance = total / 2;
        } else {
            chance = session.playerColumn.talentChance - 5;
            chance = Math.max(0, chance);
            chance = Math.min(chance, total / 2);
        }
        int roll = random.nextInt(total);
        if (roll >= chance) return false;
        addTalentPoints(player, 1);
        return true;
    }

    protected void addTalentPoints(@NonNull Player player, final int amount) {
        if (amount == 0) return;
        Session session = sessionOf(player);
        int points = session.playerColumn.talentPoints + amount;
        session.playerColumn.talentPoints = points;
        session.playerColumn.talentChance = 0;
        session.playerColumn.modified = true;
        session.saveData();
        if (amount < 1) return;
        boolean noEffect = giveAdvancement(player, null);
        int cost = session.getTalentCost();
        if (points >= cost) {
            if (!noEffect) Effects.talentUnlock(player);
            player.showTitle(Title.title(Component.text("Talent", NamedTextColor.LIGHT_PURPLE),
                                         Component.text("New Unlock Available", NamedTextColor.WHITE)));
        } else {
            if (!noEffect) Effects.talentPoint(player);
            player.showTitle(Title.title(Component.text("Talent Points", NamedTextColor.LIGHT_PURPLE),
                                         Component.text("Progress " + points + "/" + cost, NamedTextColor.WHITE)));
        }
    }

    protected void saveSQL(@NonNull Object row) {
        if (isEnabled()) {
            database.saveAsync(row, null);
        } else {
            database.save(row);
        }
    }

    protected ConfigurationSection loadYamlResource(@NonNull final String name) {
        return YamlConfiguration.loadConfiguration(new InputStreamReader(getResource(name)));
    }

    // May return null
    protected Info getInfo(String name) {
        if (infos == null) {
            infos = new HashMap<>();
            ConfigurationSection conf = loadYamlResource("infos.yml");
            for (String key : conf.getKeys(false)) {
                infos.put(key, new Info(conf.getConfigurationSection(key)));
            }
        }
        return infos.get(name);
    }

    // Never returns null
    protected TalentInfo getTalentInfo(String name) {
        if (talentInfos == null) {
            talentInfos = new HashMap<>();
            ConfigurationSection conf = loadYamlResource("talents.yml");
            for (String key : conf.getKeys(false)) {
                talentInfos.put(key, new TalentInfo(conf.getConfigurationSection(key)));
            }
        }
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
        return prettyGson.toJson(map);
    }

    protected boolean unlockTalent(@NonNull Player player, @NonNull Talent talent) {
        Session session = sessionOf(player);
        int cost = session.getTalentCost();
        if (session.getTalentPoints() < cost) return false;
        if (session.hasTalent(talent)) return false;
        if (!session.canAccessTalent(talent)) return false;
        session.playerColumn.talentPoints -= cost;
        session.talents.add(talent);
        session.playerColumn.talents = session.talents.size();
        session.playerColumn.modified = true;
        session.tag.modified = true;
        session.saveData();
        giveAdvancement(player, talent);
        return true;
    }
}
