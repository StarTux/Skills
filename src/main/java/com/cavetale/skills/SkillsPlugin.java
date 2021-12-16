package com.cavetale.skills;

import com.cavetale.skills.advancement.Advancements;
import com.cavetale.skills.session.Sessions;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.Skills;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.sql.SQLTalent;
import com.cavetale.skills.worldmarker.WorldMarkerManager;
import com.winthier.sql.SQLDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
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
    public final Guis guis = new Guis(this);
    public final Sessions sessions = new Sessions(this);
    public final Advancements advancements = new Advancements(this);
    protected final Map<String, Info> infos = new HashMap<>();
    @Getter private final WorldMarkerManager worldMarkerManager = new WorldMarkerManager(this);

    @Override
    public void onEnable() {
        instance = this;
        skillsCommand.enable();
        talentCommand.enable();
        highscoreCommand.enable();
        adminCommand.enable();
        database.registerTables(SQLSkill.class, SQLPlayer.class, SQLTalent.class);
        database.createAllTables();
        skills.enable();
        guis.enable();
        sessions.enable();
        worldMarkerManager.enable();
        advancements.createAll();
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

    protected void loadInfos() {
        for (SkillType skillType : SkillType.values()) {
            List<String> lines = new ArrayList<>();
            lines.add(skillType.tag.description());
            for (String line : skillType.tag.moreText()) {
                lines.add(line);
            }
            Info info = new Info(skillType.displayName,
                                 "Skill",
                                 String.join("\n\n", lines));
            infos.put(skillType.name().toLowerCase(), info);
        }
        for (TalentType talentType : TalentType.values()) {
            List<String> lines = new ArrayList<>();
            lines.add(talentType.tag.description());
            for (String line : talentType.tag.moreText()) {
                lines.add(line);
            }
            Info info = new Info(talentType.tag.title(),
                                 "Talent",
                                 String.join("\n\n", lines));
            infos.put(info.title.toLowerCase().replace(" ", "_"), info);
        }
    }
}
