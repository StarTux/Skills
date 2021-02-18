package com.cavetale.skills.worldmarker;

import com.cavetale.skills.Effects;
import com.cavetale.skills.Farming;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.worldmarker.block.BlockMarker;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;

/**
 * This tag is stored in a block.
 */
@RequiredArgsConstructor
public final class WateredCrop {
    private final SkillsPlugin plugin;
    private final Block block;
    transient AreaEffectCloud aoeCloud = null;
    transient String coords = null;
    static final int TICK_SPEED = 20 * 5;
    private BukkitTask task;
    private boolean debug = true;

    public void enable() {
        if (debug) log("ENABLE");
        startTask();
        updateAoeCloud();
    }

    void log(String msg) {
        plugin.getLogger().info("[WateredCrop] " + block.getWorld().getName() + ":"
                                + block.getX() + "," + block.getY() + "," + block.getZ() + " " + msg);
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
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::timer, (long) TICK_SPEED, (long) TICK_SPEED);
    }

    private void stopTask() {
        if (task == null) return;
        task.cancel();
        task = null;
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
            BlockMarker.resetId(block);
            return;
        }
        final int max = ageable.getMaximumAge();
        final int age = Math.min(max, ageable.getAge() + 1);
        ageable.setAge(age);
        block.setBlockData(ageable, true);
        if (age >= max) {
            removeAoeCloud();
            stopTask();
            BlockMarker.setId(block, Farming.GROWN_CROP);
        } else {
            Effects.cropGrow(block);
            updateAoeCloud();
        }
    }

    private void spawnAoeCloud() {
        if (aoeCloud != null) return;
        if (debug) log("SPAWN AOE");
        aoeCloud = block.getWorld().spawn(block.getLocation().add(0.5, 0.25, 0.5), AreaEffectCloud.class, e -> {
                e.setBasePotionData(new PotionData(PotionType.AWKWARD));
                e.setParticle(Particle.WATER_SPLASH);
                e.setPersistent(false);
                e.setRadius(0.5f);
                e.setDuration(TICK_SPEED + 20);
            });
    }

    private void updateAoeCloud() {
        if (aoeCloud != null && !aoeCloud.isValid()) aoeCloud = null;
        if (aoeCloud == null) spawnAoeCloud();
        aoeCloud.setTicksLived(1);
    }

    private void removeAoeCloud() {
        if (aoeCloud == null) return;
        if (debug) log("REMOVE AOE");
        aoeCloud.remove();
        aoeCloud = null;
    }
}
