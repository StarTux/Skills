package com.cavetale.skills;

import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
final class Metadata {
    private final JavaPlugin plugin;

    <T> Optional<T> get(final Metadatable entity,
                        final String key,
                        final Class<T> theClass) {
        for (MetadataValue meta : entity.getMetadata(key)) {
            if (meta.getOwningPlugin() == plugin) {
                Object value = meta.value();
                if (!theClass.isInstance(value)) {
                    return Optional.empty();
                }
                return Optional.of(theClass.cast(value));
            }
        }
        return Optional.empty();
    }

    <T> T getOrSet(final Metadatable entity,
                   final String key,
                   final Class<T> theClass,
                   final Supplier<T> dfl) {
        for (MetadataValue meta : entity.getMetadata(key)) {
            if (meta.getOwningPlugin() == plugin) {
                Object value = meta.value();
                if (theClass.isInstance(value)) {
                    return theClass.cast(value);
                }
            }
        }
        T value = dfl.get();
        set(entity, key, value);
        return value;
    }

    void set(final Metadatable entity,
             final String key,
             final Object value) {
        entity.setMetadata(key, new FixedMetadataValue(plugin, value));
    }

    void remove(final Metadatable entity, final String key) {
        entity.removeMetadata(key, plugin);
    }

    /**
     * {@link Metadatable::hasMetadata(String)} may be preferable.
     */
    boolean has(final Metadatable entity,
                final String key) {
        for (MetadataValue meta : entity.getMetadata(key)) {
            if (meta.getOwningPlugin() == plugin) {
                return true;
            }
        }
        return false;
    }
}
