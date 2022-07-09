package com.cavetale.skills;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.session.Sessions;
import com.cavetale.skills.skill.Skills;
import com.cavetale.skills.skill.archery.ArcherySkill;
import com.cavetale.skills.skill.combat.CombatSkill;
import com.cavetale.skills.skill.mining.MiningSkill;
import com.cavetale.skills.sql.SQLPlayer;
import com.cavetale.skills.sql.SQLSkill;
import com.cavetale.skills.sql.SQLTalent;
import com.cavetale.skills.util.Gui;
import com.winthier.sql.SQLDatabase;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
        this.skills = new Skills();
        this.sessions = new Sessions();
        skills.enable();
        sessions.enable();
        skillsCommand.enable();
        talentCommand.enable();
        highscoreCommand.enable();
        adminCommand.enable();
        Gui.enable();
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

    public static SkillsPlugin skillsPlugin() {
        return instance;
    }

    public static Sessions sessions() {
        return instance.sessions;
    }

    public static Session sessionOf(Player player) {
        return instance.sessions.of(player);
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
}
