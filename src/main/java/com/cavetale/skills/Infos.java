package com.cavetale.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

@RequiredArgsConstructor
final class Infos {
    private final SkillsPlugin plugin;
    private final HashMap<String, Info> infos = new HashMap<>();

    void load() {
        ConfigurationSection config = plugin.yaml.load("infos.yml");
        for (String key : config.getKeys(false)) {
            Info info = new Info(config.getConfigurationSection(key));
            infos.put(key, info);
        }
    }

    // May return null
    Info get(String name) {
        return infos.get(name);
    }

    List<String> allKeys() {
        return new ArrayList<>(infos.keySet());
    }
}
