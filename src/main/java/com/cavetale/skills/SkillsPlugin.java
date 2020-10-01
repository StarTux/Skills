package com.cavetale.skills;

import com.cavetale.skills.combat.CombatSkill;
import com.cavetale.skills.command.AdminCommand;
import com.cavetale.skills.command.SkillsCommand;
import com.cavetale.skills.farming.FarmingSkill;
import com.cavetale.skills.mining.MiningSkill;
import com.cavetale.skills.util.Gui;
import com.cavetale.skills.util.Json;
import com.cavetale.skills.util.Metadata;
import com.cavetale.skills.util.Yaml;
import com.cavetale.skills.worldmarker.WorldMarkerManager;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class SkillsPlugin extends JavaPlugin {
    @Getter static SkillsPlugin instance;
    // Utility
    final Yaml yaml = new Yaml(this);
    final Json json = new Json(this);
    final Metadata meta = new Metadata(this);
    // Listeners
    final SkillsCommand skillsCommand = new SkillsCommand(this);
    final AdminCommand adminCommand = new AdminCommand(this);
    final EventListener eventListener = new EventListener(this);
    // Skills
    final FarmingSkill farming = new FarmingSkill(this);
    final MiningSkill mining = new MiningSkill(this);
    final CombatSkill combat = new CombatSkill(this);
    // Components
    final SQL sql = new SQL(this);
    final Sessions sessions = new Sessions(this);
    final SkillPoints skillPoints = new SkillPoints(this);
    final Talents talents = new Talents(this);
    final Advancements advancements = new Advancements(this);
    final Infos infos = new Infos(this);
    final Timer timer = new Timer(this);
    final WorldMarkerManager worldMarkerManager = new WorldMarkerManager(this);
    final Menus menus = new Menus(this);

    @Override
    public void onEnable() {
        instance = this;
        skillsCommand.enable();
        adminCommand.enable();
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
        enableAllSkills();
    }

    void enableAllSkills() {
        farming.enable();
        combat.enable();
        mining.enable();
    }

    @Override
    public void onDisable() {
        sessions.disable();
        Gui.onDisable(this);
    }
}
