package com.cavetale.skills;

import com.cavetale.skills.util.Yaml;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

@RequiredArgsConstructor
public final class Infos {
    private final SkillsPlugin plugin;
    private final HashMap<String, Info> infos = new HashMap<>();

    public void load() {
        ConfigurationSection config = Yaml.loadResource("infos.yml");
        for (String key : config.getKeys(false)) {
            Info info = new Info(config.getConfigurationSection(key));
            infos.put(key, info);
        }
    }

    // May return null
    public Info get(String name) {
        return infos.get(name);
    }

    public List<String> allKeys() {
        return new ArrayList<>(infos.keySet());
    }
}
