package com.cavetale.skills;

import com.cavetale.skills.session.Sessions;
import com.cavetale.skills.skill.Skills;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.sql.SQLTalent;
import com.winthier.sql.SQLDatabase;
import java.util.List;
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

    @Override
    public void onEnable() {
        instance = this;
        database.registerTables(List.of(SQLSkill.class, SQLPlayer.class, SQLTalent.class));
        if (!database.createAllTables()) {
            throw new IllegalStateException("Database initialization failed!");
        }
        skills.enable();
        sessions.enable();
        // Commands
        skillsCommand.enable();
        talentCommand.enable();
        highscoreCommand.enable();
        adminCommand.enable();
        // UI
        guis.enable();
    }

    @Override
    public void onDisable() {
        sessions.disable();
    }

    public static int pointsForLevelUp(final int lvl) {
        return 100 * lvl;
    }

    public static int moneyBonusPercentage(final int bonus) {
        return bonus * 5;
    }
}
