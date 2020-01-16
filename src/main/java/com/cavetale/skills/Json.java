package com.cavetale.skills;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Json {
    private final JavaPlugin plugin;
    private final Gson gson = new Gson();
    private final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

    public <T> T load(final File file, final Class<T> clazz, final Supplier<T> dfl) {
        if (!file.exists()) {
            return dfl.get();
        }
        try (FileReader fr = new FileReader(file)) {
            return gson.fromJson(fr, clazz);
        } catch (FileNotFoundException fnfr) {
            return dfl.get();
        } catch (IOException ioe) {
            throw new IllegalStateException("Loading " + file, ioe);
        }
    }

    public <T> T load(final File file, final Class<T> clazz) {
        return load(file, clazz, () -> null);
    }

    public void save(final File file, final Object obj, final boolean pretty) {
        try (FileWriter fw = new FileWriter(file)) {
            Gson gs = pretty ? prettyGson : gson;
            gs.toJson(obj, fw);
        } catch (IOException ioe) {
            throw new IllegalStateException("Saving " + file, ioe);
        }
    }

    public String serialize(Object o) {
        return gson.toJson(o);
    }

    public String pretty(Object o) {
        return prettyGson.toJson(o);
    }

    public <T> T deserialize(String inp, Class<T> clazz, Supplier<T> dfl) {
        if (inp == null) return dfl.get();
        try {
            return gson.fromJson(inp, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return dfl.get();
        }
    }

    public <T> T deserialize(String inp, Class<T> clazz) {
        return deserialize(inp, clazz, () -> null);
    }
}
