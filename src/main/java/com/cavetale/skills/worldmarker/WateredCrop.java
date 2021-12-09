package com.cavetale.skills.worldmarker;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.farming.FarmingSkill;
import com.cavetale.skills.util.Effects;
import com.cavetale.worldmarker.block.BlockMarker;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitTask;

/**
 * This tag is stored in a block.
 */
@RequiredArgsConstructor
public final class WateredCrop {
    private final SkillsPlugin plugin;
    protected final Block block;
    static final int TICK_SPEED = 20 * 60;
    private BukkitTask task;
    private boolean debug = false;
    protected transient long particleCooldown;

    public void enable() {
        if (debug) log("ENABLE");
        startTask();
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
            stopTask();
            BlockMarker.setId(block, FarmingSkill.GROWN_CROP);
        } else {
            Effects.cropGrow(block);
        }
    }
}
