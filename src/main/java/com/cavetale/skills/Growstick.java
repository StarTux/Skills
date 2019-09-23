package com.cavetale.skills;

import com.cavetale.worldmarker.BlockMarker;
import lombok.NonNull;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Object to manage growstick and watered block related features.
 * Called by EventListener et al, owned by SkillsPlugin.
 */
final class Growstick {
    final SkillsPlugin plugin;

    /**
     * Player uses a growstick on a certain block.
     */
    void use(@NonNull Player player, @NonNull Block block) {
        int radius = 1; // ;-)
        
    }
}
