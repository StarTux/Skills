package com.cavetale.skills.util;

import com.cavetale.skills.SkillsPlugin;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.function.Supplier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Yaml {
    private static final Gson GSON = new Gson();
    private static final org.yaml.snakeyaml.Yaml YAML = new org.yaml.snakeyaml.Yaml();

    private Yaml() { }

    /**
     * Load a config file from the config folder, using the resource
     * that comes with the package jar as default.  Save the resource
     * to disk if it does not already exist.
     */
    public static ConfigurationSection load(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config;
    }

    public static ConfigurationSection loadResource(String path) {
        SkillsPlugin plugin = SkillsPlugin.getInstance();
        InputStream inputStream = plugin.getResource(path);
        if (inputStream == null) throw new IllegalStateException("null: " + path);
        Reader reader = new InputStreamReader(inputStream);
        return YamlConfiguration.loadConfiguration(reader);
    }

    public static <E> E load(File file, Class<E> clazz, Supplier<E> dfl) {
        Object o;
        try (FileReader in = new FileReader(file)) {
            o = YAML.load(in);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return dfl.get();
        }
        String tmp = GSON.toJson(o);
        return GSON.fromJson(tmp, clazz);
    }
}
