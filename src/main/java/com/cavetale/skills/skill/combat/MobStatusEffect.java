package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.worldmarker.util.Tags;
import java.time.Duration;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataContainer;

/**
 * A status effect applicable to a mob.
 */
public enum MobStatusEffect {
    NO_POISON,
    SILENCE;

    private NamespacedKey key;

    protected static void enable(SkillsPlugin plugin) {
        for (MobStatusEffect it : MobStatusEffect.values()) {
            it.key = new NamespacedKey(plugin, it.name().toLowerCase());
        }
    }

    public boolean has(Mob mob) {
        PersistentDataContainer pdc = mob.getPersistentDataContainer();
        Long value = Tags.getLong(pdc, key);
        if (value == null) return false;
        if (value > System.currentTimeMillis()) return true;
        pdc.remove(key);
        return false;
    }

    public void set(Mob mob, Duration duration) {
        PersistentDataContainer pdc = mob.getPersistentDataContainer();
        Tags.set(pdc, key, System.currentTimeMillis() + duration.toMillis());
    }

}
