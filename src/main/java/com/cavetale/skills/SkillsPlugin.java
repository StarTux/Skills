package com.cavetale.skills;

import com.cavetale.skills.session.Sessions;
import com.cavetale.skills.skill.Skills;
import com.cavetale.skills.skill.archery.ArcherySkill;
import com.cavetale.skills.skill.combat.CombatSkill;
import com.cavetale.skills.skill.mining.MiningSkill;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.sql.SQLTalent;
import com.winthier.sql.SQLDatabase;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class SkillsPlugin extends JavaPlugin {
    private static SkillsPlugin instance;
    protected SkillsCommand skillsCommand = new SkillsCommand(this);
    protected TalentCommand talentCommand = new TalentCommand(this);
    protected HighscoreCommand highscoreCommand = new HighscoreCommand(this);
    protected AdminCommand adminCommand = new AdminCommand(this);
    protected final Random random = ThreadLocalRandom.current();
    protected final SQLDatabase database = new SQLDatabase(this);
    protected Skills skills;
    protected Sessions sessions;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        database.registerTables(List.of(SQLSkill.class, SQLPlayer.class, SQLTalent.class));
        if (!database.createAllTables()) {
            throw new IllegalStateException("Database initialization failed!");
        }
        database.getTable(SQLTalent.class).createColumnIfMissing("level");
        this.skills = new Skills();
        this.sessions = new Sessions();
        skills.enable();
        sessions.enable();
        skillsCommand.enable();
        talentCommand.enable();
        highscoreCommand.enable();
        adminCommand.enable();
    }

    @Override
    public void onDisable() {
        sessions.disable();
    }

    public static int pointsForLevelUp(final int lvl) {
        return 100 * lvl;
    }

    public static SkillsPlugin skillsPlugin() {
        return instance;
    }

    public static Skills skills() {
        return instance.skills;
    }

    public static CombatSkill combatSkill() {
        return instance.skills.combat;
    }

    public static MiningSkill miningSkill() {
        return instance.skills.mining;
    }

    public static ArcherySkill archerySkill() {
        return instance.skills.archery;
    }

    public static Random random() {
        return instance.random;
    }

    public static SQLDatabase database() {
        return instance.database;
    }

    public static SkillsCommand skillsCommand() {
        return instance.skillsCommand;
    }

    public static NamespacedKey namespacedKey(String key) {
        return new NamespacedKey(instance, key);
    }
}
