package com.cavetale.skills;

import com.cavetale.skills.advancement.Advancements;
import com.cavetale.skills.session.Sessions;
import com.cavetale.skills.skill.Skills;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.worldmarker.WorldMarkerManager;
import com.winthier.sql.SQLDatabase;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkillsPlugin extends JavaPlugin {
    @Getter protected static SkillsPlugin instance;
    protected SkillsCommand skillsCommand = new SkillsCommand(this);
    protected TalentCommand talentCommand = new TalentCommand(this);
    protected HighscoreCommand highscoreCommand = new HighscoreCommand(this);
    protected AdminCommand adminCommand = new AdminCommand(this);
    public final Random random = ThreadLocalRandom.current();
    public final SQLDatabase database = new SQLDatabase(this);
    public final Skills skills = new Skills(this);
    public final Sessions sessions = new Sessions(this);
    public final Advancements advancements = new Advancements(this);
    protected final Map<String, TalentInfo> talentInfos = new HashMap<>();
    protected final Map<String, Info> infos = new HashMap<>();
    @Getter private final WorldMarkerManager worldMarkerManager = new WorldMarkerManager(this);

    @Override
    public void onEnable() {
        instance = this;
        skillsCommand.enable();
        talentCommand.enable();
        highscoreCommand.enable();
        adminCommand.enable();
        database.registerTables(SQLSkill.class, SQLPlayer.class);
        database.createAllTables();
        skills.enable();
        sessions.enable();
        worldMarkerManager.enable();
        loadInfos();
        advancements.createAll();
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

    protected void loadInfos() {
        ConfigurationSection conf;
        infos.clear();
        conf = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("infos.yml")));
        for (String key : conf.getKeys(false)) {
            infos.put(key, new Info(conf.getConfigurationSection(key)));
        }
        talentInfos.clear();
        conf = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("talents.yml")));
        for (String key : conf.getKeys(false)) {
            talentInfos.put(key, new TalentInfo(conf.getConfigurationSection(key)));
        }
    }

    // Never returns null
    public TalentInfo getTalentInfo(String name) {
        TalentInfo result = talentInfos.get(name);
        if (result == null) {
            getLogger().warning("Missing talent info: " + name);
            result = new TalentInfo(name);
            talentInfos.put(name, result);
        }
        return result;
    }
}
