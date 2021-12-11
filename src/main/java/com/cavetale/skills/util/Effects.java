package com.cavetale.skills.util;

import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

/**
 * Utility class to launch sound and particle effects.
 */
public final class Effects {
    private Effects() { }

    public static void wateredCropAmbient(@NonNull Block block) {
        block.getWorld().spawnParticle(Particle.DRIP_WATER,
                                       block.getLocation().add(0.5, 0.125, 0.5),
                                       1, // count
                                       0.20, 0.1, 0.20, // offset
                                       0.0); // extra/speed
    }

    public static void grownCropAmbient(@NonNull Player player, @NonNull Block block) {
        player.spawnParticle(Particle.BLOCK_DUST,
                             block.getLocation().add(0.5, 0.25, 0.5),
                             1, // count
                             0.1, 0.1, 0.1, // offset
                             0.0, // extra/speed
                             block.getBlockData());
    }

    public static void waterBlock(@NonNull Block block) {
        World w = block.getWorld();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        w.spawnParticle(Particle.WATER_SPLASH,
                        loc,
                        38, // count
                        0.2, 0.2, 0.2, // offset
                        0.0); // extra/speed
        w.playSound(loc, Sound.ENTITY_BOAT_PADDLE_WATER, SoundCategory.BLOCKS, 1.0f, 1.5f);
    }

    public static void wateringCan(@NonNull Player player) {
        World w = player.getWorld();
        Location loc = player.getEyeLocation();
        w.playSound(loc, Sound.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.6f, 1.7f);
        w.playSound(loc, Sound.WEATHER_RAIN, SoundCategory.BLOCKS, 0.3f, 2.0f);
    }

    public static void cropGrow(@NonNull Block block) {
        World w = block.getWorld();
        w.spawnParticle(Particle.VILLAGER_HAPPY,
                        block.getLocation().add(0.5, 0.5, 0.5),
                        2, // count
                        0.25, 0.25, 0.25, // offset
                        0.0); // extra/speed
    }

    public static void cropUnlit(@NonNull Block block) {
        World w = block.getWorld();
        w.spawnParticle(Particle.SMOKE_NORMAL,
                        block.getLocation().add(0.5, 0.5, 0.5),
                        2, // count
                        0.25, 0.25, 0.25, // offset
                        0.0); // extra/speed
    }

    public static void plantCropMagic(@NonNull Block block) {
        World w = block.getWorld();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        w.spawnParticle(Particle.VILLAGER_HAPPY,
                        loc,
                        2, // count
                        0.25, 0.25, 0.25, // offset
                        0.0); // extra/speed
        w.playSound(loc, Sound.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 0.8f, 1.0f);
    }

    public static void rewardJingle(@NonNull Location location) {
        World w = location.getWorld();
        w.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.5f, 2.0f);
    }

    public static void levelup(@NonNull Player player) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    public static void talentPoint(@NonNull Player player) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    public static void talentUnlock(@NonNull Player player) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    public static void harvest(@NonNull Block block) {
        World w = block.getWorld();
        w.spawnParticle(Particle.END_ROD,
                        block.getLocation().add(0.5, 0.5, 0.5),
                        8, // count
                        0.25, 0.25, 0.25, // offset
                        0.25); // extra/speed
    }

    public static void warp(@NonNull Entity e) {
        World w = e.getWorld();
        BoundingBox bb = e.getBoundingBox();
        Location loc = bb.getCenter().toLocation(w);
        w.spawnParticle(Particle.REDSTONE,
                        loc,
                        32,
                        bb.getWidthX() * 0.5, bb.getHeight() * 0.5, bb.getWidthZ() * 0.5, // offset
                        0.0, // extra/speed (REDSTONE does not care)
                        new Particle.DustOptions(Color.PURPLE, 2.0f));
        w.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0f, 1.0f);
    }

    public static void mineBlockMagic(@NonNull Block block) {
        World w = block.getWorld();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        w.spawnParticle(Particle.BLOCK_DUST,
                        loc,
                        16, // count
                        0.25, 0.25, 0.25, // offset
                        0.0, // extra/speed
                        block.getBlockData());
        w.playSound(loc, Sound.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0f, 1.5f);
    }

    public static void oreAlert(@NonNull Block block) {
        World w = block.getWorld();
        w.playSound(block.getLocation().add(0.5, 0.5, 0.5),
                    Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.BLOCKS, 1.0f, 2.0f);
    }

    public static void useSilk(@NonNull Player player, @NonNull Block block, @NonNull Location loc) {
        World w = loc.getWorld();
        w.playSound(loc,
                    Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.BLOCKS,
                    1.0f, 2.0f);
        w.spawnParticle(Particle.BLOCK_DUST,
                        loc,
                        3, // count
                        0.0, 0.0, 0.0, // offset
                        0.0, // extra/speed
                        block.getBlockData());
    }

    public static void failSilk(@NonNull Player player, @NonNull Block block) {
        World w = block.getWorld();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        w.playSound(loc,
                    Sound.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS,
                    1.0f, 2.0f);
    }

    public static void applyStatusEffect(@NonNull LivingEntity entity) {
        World w = entity.getWorld();
        Location loc = entity.getEyeLocation();
        w.spawnParticle(Particle.ENCHANTMENT_TABLE,
                        loc,
                        64, // count
                        0.4, 0.4, 0.4, // offset
                        1.0); // extra/speed
        w.playSound(loc,
                    Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS,
                    0.5f, 1.8f);
    }

    public static void godMode(@NonNull Player player) {
        Location loc = player.getEyeLocation();
        player.playSound(loc,
                         Sound.ITEM_TOTEM_USE, SoundCategory.MASTER,
                         0.5f, 1.5f);
        player.spawnParticle(Particle.TOTEM,
                             loc, 32, // count
                             0, 0, 0, // offset
                             0.35); // speed
    }

    public static void archerZone(@NonNull Player player) {
        player.playSound(player.getEyeLocation(),
                         Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.MASTER,
                         0.3f, 1.0f);
    }

    public static void denyLaunch(@NonNull LivingEntity entity) {
        World w = entity.getWorld();
        Location loc = entity.getEyeLocation();
        w.spawnParticle(Particle.ENCHANTMENT_TABLE,
                        loc,
                        4, // count
                        0.1, 0.1, 0.1, // offset
                        1.0); // extra/speed
    }

    public static void hoe(@NonNull Block block, @NonNull BlockData old) {
        World w = block.getWorld();
        if (old.getMaterial() == Material.GRASS_BLOCK) {
            old = Material.GRASS.createBlockData();
        }
        w.spawnParticle(Particle.BLOCK_DUST,
                        block.getLocation().add(0.5, 1.0, 0.5),
                        20, // count
                        0.2, 0.0, 0.2, // offset
                        0.0, // extra/speed
                        old);
    }
}
