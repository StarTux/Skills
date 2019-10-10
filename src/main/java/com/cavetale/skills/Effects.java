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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

/**
 * Utility class to launch sound and particle effects.
 */
final class Effects {
    private Effects() { }

    static void wateredCropAmbient(@NonNull Block block) {
        World w = block.getWorld();
        w.spawnParticle(Particle.DRIP_WATER,
                        block.getLocation().add(0.5, 0.125, 0.5),
                        1, // count
                        0.20, 0.1, 0.20, // offset
                        0.0); // extra/speed
    }

    static void grownCropAmbient(@NonNull Block block) {
        World w = block.getWorld();
        w.spawnParticle(Particle.BLOCK_DUST,
                        block.getLocation().add(0.5, 0.25, 0.5),
                        1, // count
                        0.1, 0.1, 0.1, // offset
                        0.0, // extra/speed
                        block.getBlockData());
    }

    static void waterBlock(@NonNull Block block) {
        World w = block.getWorld();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        w.spawnParticle(Particle.WATER_SPLASH,
                        loc,
                        38, // count
                        0.2, 0.2, 0.2, // offset
                        0.0); // extra/speed
        w.playSound(loc, Sound.ENTITY_BOAT_PADDLE_WATER, SoundCategory.BLOCKS, 1.0f, 1.5f);
    }

    static void wateringCan(@NonNull Player player) {
        World w = player.getWorld();
        Location loc = player.getEyeLocation();
        w.playSound(loc, Sound.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.6f, 1.7f);
        w.playSound(loc, Sound.WEATHER_RAIN, SoundCategory.BLOCKS, 0.3f, 2.0f);
    }

    static void cropGrow(@NonNull Block block) {
        World w = block.getWorld();
        w.spawnParticle(Particle.VILLAGER_HAPPY,
                        block.getLocation().add(0.5, 0.5, 0.5),
                        2, // count
                        0.25, 0.25, 0.25, // offset
                        0.0); // extra/speed
    }

    static void plantCropMagic(@NonNull Block block) {
        World w = block.getWorld();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        w.spawnParticle(Particle.VILLAGER_HAPPY,
                        loc,
                        2, // count
                        0.25, 0.25, 0.25, // offset
                        0.0); // extra/speed
        w.playSound(loc, Sound.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 0.8f, 1.0f);
    }

    static void rewardJingle(@NonNull Location location) {
        World w = location.getWorld();
        w.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.5f, 2.0f);
    }

    static void levelup(@NonNull Player player) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    static void talentPoint(@NonNull Player player) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    static void talentUnlock(@NonNull Player player) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1.0f, 1.0f);
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
        w.spawnParticle(Particle.REDSTONE,
                        bb.getCenter().toLocation(w),
                        48,
                        bb.getWidthX() * 0.35, bb.getHeight() * 0.35, bb.getWidthZ() * 0.35, // offset
                        0.0, // extra/speed (REDSTONE does not care)
                        new Particle.DustOptions(Color.RED, 2.0f));
    }

    static void bossSwirl(@NonNull Entity e, final int tick) {
        World w = e.getWorld();
        BoundingBox bb = e.getBoundingBox();
        double t = (double) tick * 0.2;
        Location loc = bb.getCenter().toLocation(w)
            .add(Math.cos(t) * bb.getWidthX() * 1.125,
                 Math.sin(t * 1.5) * bb.getHeight() * 0.25,
                 Math.sin(t) * bb.getWidthZ() * 1.125);
        w.spawnParticle(Particle.REDSTONE,
                        loc,
                        1,
                        0, 0, 0, // offset
                        0.0, // extra/speed (REDSTONE does not care)
                        new Particle.DustOptions(Color.BLUE, 1.0f));
    }

    static void warp(@NonNull Entity e) {
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

    static void mineBlockMagic(@NonNull Block block) {
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

    static void oreAlert(@NonNull Player player, @NonNull Block block) {
        player.playSound(block.getLocation().add(0.5, 0.5, 0.5),
                         Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.BLOCKS, 1.0f, 2.0f);
    }

    static void xray(@NonNull Player player) {
        player.playSound(player.getEyeLocation(),
                         Sound.ENTITY_ILLUSIONER_MIRROR_MOVE,
                         SoundCategory.MASTER,
                         0.5f, 1.5f);
    }

    static void xray(@NonNull Player player, @NonNull Block block) {
        player.spawnParticle(Particle.END_ROD,
                             block.getLocation().add(0.5, 0.5, 0.5),
                             1, // count
                             0.1, 0.1, 0.1, // offset
                             0.05); // extra/speed
    }

    static void useSilk(@NonNull Player player, @NonNull Block block, @NonNull Location loc) {
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

    static void failSilk(@NonNull Player player, @NonNull Block block) {
        World w = block.getWorld();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        w.playSound(loc,
                    Sound.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS,
                    1.0f, 2.0f);
    }

    static void applyStatusEffect(@NonNull LivingEntity entity) {
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

    static void godMode(@NonNull Player player) {
        Location loc = player.getEyeLocation();
        player.playSound(loc,
                         Sound.ITEM_TOTEM_USE, SoundCategory.MASTER,
                         0.5f, 1.5f);
        player.spawnParticle(Particle.TOTEM,
                             loc, 32, // count
                             0, 0, 0, // offset
                             0.35); // speed
    }

    static void archerZone(@NonNull Player player) {
        player.playSound(player.getEyeLocation(),
                         Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.MASTER,
                         0.3f, 1.0f);
    }

    static void denyLaunch(@NonNull LivingEntity entity) {
        World w = entity.getWorld();
        Location loc = entity.getEyeLocation();
        w.spawnParticle(Particle.ENCHANTMENT_TABLE,
                        loc,
                        4, // count
                        0.1, 0.1, 0.1, // offset
                        1.0); // extra/speed
    }
}
