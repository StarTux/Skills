package com.cavetale.skills.skill.combat;

import com.cavetale.worldmarker.util.Tags;
import java.time.Duration;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataContainer;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

/**
 * A status effect applicable to a mob.
 * The helper methods an expiry time code in the entity tag.
 */
public enum MobStatusEffect {
    DENIAL;

    private NamespacedKey key;

    protected static void enable() {
        for (MobStatusEffect it : MobStatusEffect.values()) {
            it.key = new NamespacedKey(skillsPlugin(), it.name().toLowerCase());
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
