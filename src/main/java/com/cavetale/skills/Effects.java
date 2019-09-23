package com.cavetale.skills;

import lombok.NonNull;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Utility class to launch sound and particle effects.
 */
final class Effects {
    private Effects() { }

    void waterBlock(@NonNull Block block) {
        World w = block.getWorld();
        w.spawnParticle(Particle.DRIP_WATER,
                        block.getCenterLocation(),
                        16, // count
                        0.5, 0.5, 0.5, // offset
                        0.0); // extra/speed
    }
}
