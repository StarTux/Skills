package com.cavetale.skills;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkillsPlugin extends JavaPlugin {
    SkillsCommand skillsCommand = new SkillsCommand(this);
    EventListener eventListener = new EventListener(this);
    Growstick growstick = new Growstick(this);
    Random random = ThreadLocalRandom.current();

    @Override
    public void onEnable() {
        getCommand("skills").setExecutor(skillsCommand);
        getServer().getPluginManager().registerEvents(eventListener, this);
    }

    @Override
    public void onDisable() {
    }
}
