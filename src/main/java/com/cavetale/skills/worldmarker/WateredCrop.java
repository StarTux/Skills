package com.cavetale.skills.worldmarker;

import com.cavetale.skills.Effects;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.util.Blocks;
import com.cavetale.worldmarker.WorldMarkerPlugin;
import com.cavetale.worldmarker.block.BlockMarker;
import com.cavetale.worldmarker.util.Tags;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;

/**
 * This tag is stored in a block.
 */
@RequiredArgsConstructor
public final class WateredCrop {
    private final Block block;
    transient AreaEffectCloud aoeCloud = null;
    transient String coords = null;
    int water = 0;
    static final int MAX_WATER = 7;
    static final int TICK_SPEED = 20 * 60;
    static NamespacedKey waterKey;
    private BukkitTask task;
    private boolean debug;

    public static void init() {
        waterKey = new NamespacedKey(SkillsPlugin.getInstance(), "water");
    }

    public void load() {
        BlockMarker.getTag(block, false, tag -> {
                Integer w = Tags.getInt(tag, waterKey);
                this.water = w != null ? w : 0;
                return false;
            });
    }

    public void enable() {
        if (debug) log("ENABLE");
        if (!isFullyGrown()) {
            startTask();
            updateAoeCloud();
        }
    }

    void log(String msg) {
        SkillsPlugin.getInstance().getLogger().info("[WateredCrop] " + Blocks.toString(block) + " " + msg);
    }

    /**
     * Called when this instance is removed so it clears up resources.
     */
    public void disable() {
        if (debug) log("DISABLE");
        stopTask();
        removeAoeCloud();
    }

    private void startTask() {
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(SkillsPlugin.getInstance(), this::timer, (long) TICK_SPEED, (long) TICK_SPEED);
    }

    private void stopTask() {
        if (task == null) return;
        task.cancel();
        task = null;
    }

    /**
     * Remove from world.
     */
    public void remove() {
        BlockMarker.getTag(block, false, tag -> {
                tag.remove(WorldMarkerPlugin.ID_KEY);
                tag.remove(waterKey);
                return true;
            });
    }

    public Ageable getAgeable() {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Ageable)) return null;
        return (Ageable) blockData;
    }

    public boolean isFullyGrown() {
        Ageable ageable = getAgeable();
        if (ageable == null) return false;
        return ageable.getAge() >= ageable.getMaximumAge();
    }

    public void timer() {
        Ageable ageable = getAgeable();
        if (ageable == null) {
            SkillsPlugin.getInstance().getWorldMarkerManager().removeWateredCrop(block);
            return;
        }
        final int max = ageable.getMaximumAge();
        final int age = ageable.getAge();
        if (age >= max) {
            removeAoeCloud();
            stopTask();
            return;
        }
        if (water > 0) {
            water -= 1;
            BlockMarker.getTag(block, true, tag -> {
                    Tags.set(tag, waterKey, water);
                    return true;
                });
            if (age < max) {
                ageable.setAge(age + 1);
                block.setBlockData(ageable, true);
                Effects.cropGrow(block);
            }
        }
        updateAoeCloud();
    }

    private void spawnAoeCloud() {
        if (aoeCloud != null) return;
        if (debug) log("SPAWN AOE");
        aoeCloud = block.getWorld().spawn(block.getLocation().add(0.5, 0.25, 0.5), AreaEffectCloud.class, e -> {
                e.setBasePotionData(new PotionData(PotionType.AWKWARD));
                updateParticle(e, water);
                e.setPersistent(false);
                e.setRadius(0.5f);
                e.setDuration(TICK_SPEED + 20);
            });
    }

    private void updateAoeCloud() {
        if (aoeCloud != null && !aoeCloud.isValid()) aoeCloud = null;
        if (aoeCloud == null) {
            if (water <= 0) return;
            spawnAoeCloud();
        }
        if (water <= 0) {
            removeAoeCloud();
            return;
        }
        updateParticle(aoeCloud, water);
        aoeCloud.setTicksLived(1);
    }

    static void updateParticle(AreaEffectCloud cloud, int water) {
        if (water <= 2) {
            cloud.setParticle(Particle.WATER_WAKE);
        } else if (water <= 5) {
            cloud.setParticle(Particle.WATER_DROP);
        } else {
            cloud.setParticle(Particle.WATER_SPLASH);
        }
    }

    private void removeAoeCloud() {
        if (aoeCloud == null) return;
        if (debug) log("REMOVE AOE");
        aoeCloud.remove();
        aoeCloud = null;
    }

    public boolean water(Player player) {
        if (water >= MAX_WATER) return false;
        water = MAX_WATER;
        BlockMarker.getTag(block, true, tag -> {
                Tags.set(tag, waterKey, water);
                return true;
            });
        startTask();
        updateAoeCloud();
        return true;
    }
}
