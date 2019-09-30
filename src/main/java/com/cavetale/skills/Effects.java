package com.cavetale.skills;

import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

/**
 * Utility class to launch sound and particle effects.
 */
final class Effects {
    private Effects() { }

    static void wateredBlockAmbient(@NonNull Block block) {
        World w = block.getWorld();
        w.spawnParticle(Particle.DRIP_WATER,
                        block.getLocation().add(0.5, 0.125, 0.5),
                        1, // count
                        0.20, 0.1, 0.20, // offset
                        0.0); // extra/speed
    }

    static void grownBlockAmbient(@NonNull Block block) {
        World w = block.getWorld();
        w.spawnParticle(Particle.END_ROD,
                        block.getLocation().add(0.5, 0.5, 0.5),
                        1, // count
                        0.20, 0.20, 0.20, // offset
                        0.0); // extra/speed
    }

    static void waterBlock(@NonNull Block block) {
        World w = block.getWorld();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        w.spawnParticle(Particle.WATER_SPLASH,
                        loc,
                        32, // count
                        0.25, 0.25, 0.25, // offset
                        0.0); // extra/speed
        w.playSound(loc, Sound.ENTITY_BOAT_PADDLE_WATER, SoundCategory.BLOCKS, 1.0f, 1.5f);
    }

    static void wateringCan(@NonNull Player player) {
        World w = player.getWorld();
        Location loc = player.getEyeLocation();
        w.playSound(loc, Sound.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.6f, 1.7f);
    }

    static void cropGrow(@NonNull Block block) {
        World w = block.getWorld();
        w.spawnParticle(Particle.VILLAGER_HAPPY,
                        block.getLocation().add(0.5, 0.5, 0.5),
                        2, // count
                        0.25, 0.25, 0.25, // offset
                        0.0); // extra/speed
    }

    static void rewardJingle(@NonNull Location location) {
        World w = location.getWorld();
        w.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.5f, 2.0f);
    }

    static void harvest(@NonNull Block block) {
        World w = block.getWorld();
        w.spawnParticle(Particle.END_ROD,
                        block.getLocation().add(0.5, 0.5, 0.5),
                        8, // count
                        0.25, 0.25, 0.25, // offset
                        0.25); // extra/speed
    }

    static void kill(@NonNull Entity e) {
        World w = e.getWorld();
        BoundingBox bb = e.getBoundingBox();
        int amount = (int) (bb.getVolume() * 32);
        w.spawnParticle(Particle.REDSTONE,
                        bb.getCenter().toLocation(w),
                        amount,
                        bb.getWidthX() * 0.5, bb.getHeight() * 0.5, bb.getWidthZ() * 0.5, // offset
                        0.0, // extra/speed (REDSTONE does not care)
                        new Particle.DustOptions(Color.RED, 1.0f));
    }
}
