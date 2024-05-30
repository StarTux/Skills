package com.cavetale.skills.util;

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
public final class Effects {
    private Effects() { }

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
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.25f, 2.0f);
    }

    public static void warp(@NonNull Entity e) {
        World w = e.getWorld();
        BoundingBox bb = e.getBoundingBox();
        Location loc = bb.getCenter().toLocation(w);
        w.spawnParticle(Particle.DUST,
                        loc,
                        32,
                        bb.getWidthX() * 0.5, bb.getHeight() * 0.5, bb.getWidthZ() * 0.5, // offset
                        0.0, // extra/speed (DUST does not care)
                        new Particle.DustOptions(Color.PURPLE, 2.0f));
        w.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0f, 1.0f);
    }

    public static void mineBlockMagic(@NonNull Block block) {
        World w = block.getWorld();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        w.spawnParticle(Particle.BLOCK,
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

    public static void emeraldAlert(@NonNull Block block) {
        World w = block.getWorld();
        w.playSound(block.getLocation().add(0.5, 0.5, 0.5),
                    Sound.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.BLOCKS, 2.0f, 1.0f);
    }

    public static void debrisAlert(@NonNull Block block) {
        World w = block.getWorld();
        w.playSound(block.getLocation().add(0.5, 0.5, 0.5),
                    Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.BLOCKS, 1.0f, 2.0f); // placeholder effect, should not clash with the Diamond Ore sound
    }

    public static void useSilk(@NonNull Player player, @NonNull Block block, @NonNull Location loc) {
        World w = loc.getWorld();
        w.playSound(loc,
                    Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.BLOCKS,
                    1.0f, 2.0f);
        w.spawnParticle(Particle.BLOCK,
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

    public static void godMode(@NonNull Player player) {
        Location loc = player.getEyeLocation();
        player.playSound(loc,
                         Sound.ITEM_TOTEM_USE, SoundCategory.MASTER,
                         0.5f, 1.5f);
        player.spawnParticle(Particle.TOTEM_OF_UNDYING,
                             loc, 32, // count
                             0, 0, 0, // offset
                             0.35); // speed
    }
}
