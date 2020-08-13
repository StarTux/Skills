package com.cavetale.skills;

import com.cavetale.skills.worldmarker.WorldMarkerManager;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class SkillsPlugin extends JavaPlugin {
    @Getter static SkillsPlugin instance;
    // Utility
    final Random random = ThreadLocalRandom.current();
    final Yaml yaml = new Yaml(this);
    final Json json = new Json(this);
    final Metadata meta = new Metadata(this);
    // Listeners
    final SkillsCommand skillsCommand = new SkillsCommand(this);
    final AdminCommand adminCommand = new AdminCommand(this);
    final EventListener eventListener = new EventListener(this);
    // Skills
    final Farming farming = new Farming(this);
    final Mining mining = new Mining(this);
    final Combat combat = new Combat(this);
    // Components
    final SQL sql = new SQL(this);
    final Sessions sessions = new Sessions(this);
    final Points points = new Points(this);
    final Talents talents = new Talents(this);
    final Advancements advancements = new Advancements(this);
    final Infos infos = new Infos(this);
    final Timer timer = new Timer(this);
    final WorldMarkerManager worldMarkerManager = new WorldMarkerManager(this);

    @Override
    public void onEnable() {
        instance = this;
        skillsCommand.enable();
        adminCommand.enable();
        SkillType.setup();
        Talent.setup();
        sql.enable();
        sql.loadDatabase();
        getServer().getPluginManager().registerEvents(eventListener, this);
        worldMarkerManager.enable();
        advancements.loadAll();
        infos.load();
        for (Player player : getServer().getOnlinePlayers()) {
            sessions.load(player);
        }
        getServer().getPluginManager().registerEvents(new Gui.EventListener(), this);
        timer.start();
    }

    @Override
    public void onDisable() {
        sessions.disable();
        Gui.onDisable(this);
    }
}
