package com.cavetale.skills;

import com.cavetale.worldmarker.BlockMarker;
import com.cavetale.worldmarker.MarkBlock;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Timer object to run stuff every tick.
 * Owned by SkillsPlugin.
 */
@RequiredArgsConstructor
public final class Timer {
    private final SkillsPlugin plugin;
    private long ticks = 0;

    void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::onTick, 1, 1);
    }

    void onTick() {
        ticks += 1;
        plugin.sessions.tick();
        if ((ticks % 10) == 0) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                tickPlayer(player);
            }
        }
    }

    // Show ambient particle effects of nearby blocks
    void tickPlayer(@NonNull Player player) {
        if (plugin.sessions.of(player).noParticles) return;
        List<MarkBlock> blocks =
            BlockMarker.getNearbyBlocks(player.getLocation().getBlock(), 24)
            .stream().filter(mb -> mb.hasId() && mb.getId().startsWith("skills:"))
            .collect(Collectors.toList());
        if (blocks.isEmpty()) return;
        Collections.shuffle(blocks, plugin.random);
        final int max = Math.min(blocks.size(), 48);
        for (int i = 0; i < max; i += 1) {
            MarkBlock markBlock = blocks.get(i);
            switch (markBlock.getId()) {
            case Farming.WATERED_CROP:
                Effects.wateredCropAmbient(player, markBlock.getBlock());
                break;
            case Farming.GROWN_CROP:
                Effects.grownCropAmbient(player, markBlock.getBlock());
                break;
            default: break;
            }
        }
    }
}
