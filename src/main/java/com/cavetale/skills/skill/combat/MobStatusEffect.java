package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.worldmarker.util.Tags;
import java.time.Duration;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitRunnable;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

/**
 * A status effect applicable to a mob.
 * The helper methods an expiry time code in the entity tag.
 */
public enum MobStatusEffect {
    DENIAL,
    FREEZE,
    ;

    private NamespacedKey key;

    protected static void enableAll() {
        for (MobStatusEffect it : MobStatusEffect.values()) {
            it.key = SkillsPlugin.namespacedKey(it.name().toLowerCase());
        }
    }

    public boolean has(Mob mob) {
        final PersistentDataContainer pdc = mob.getPersistentDataContainer();
        final Long value = Tags.getLong(pdc, key);
        if (value == null) return false;
        if (value > System.currentTimeMillis()) return true;
        remove(mob);
        return false;
    }

    public void set(Mob mob, Duration duration) {
        PersistentDataContainer pdc = mob.getPersistentDataContainer();
        Tags.set(pdc, key, System.currentTimeMillis() + duration.toMillis());
        if (this == FREEZE) {
            mob.setAware(false);
        }
    }

    public void remove(Mob mob) {
        final PersistentDataContainer pdc = mob.getPersistentDataContainer();
        pdc.remove(key);
        if (this == FREEZE) {
            mob.setAware(true);
        }
    }

    public void tick(Mob mob, Duration duration) {
        set(mob, duration);
        new BukkitRunnable() {
            @Override public void run() {
                if (mob.isDead() || !has(mob)) {
                    // Removed
                    cancel();
                    return;
                }
                if (MobStatusEffect.this == FREEZE) {
                    mob.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, mob.getEyeLocation(), 1, 0.25, 0.25, 0.25, 0.125);
                }
            }
        }.runTaskTimer(skillsPlugin(), 1L, 1L);
    }
}
