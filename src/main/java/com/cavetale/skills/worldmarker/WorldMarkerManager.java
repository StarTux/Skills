package com.cavetale.skills.worldmarker;

import com.cavetale.skills.Farming;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.worldmarker.BlockMarker;
import com.cavetale.worldmarker.MarkChunk;
import com.cavetale.worldmarker.MarkChunkLoadEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public final class WorldMarkerManager implements Listener {
    final SkillsPlugin plugin;

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        BlockMarker.streamAllLoadedChunks().forEach(this::handleChunk);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    void onMarkChunkLoad(MarkChunkLoadEvent event) {
        handleChunk(event.getChunk());
    }

    private void handleChunk(MarkChunk markChunk) {
        markChunk.streamBlocksWithId(MarkerId.WATERED_CROP.key)
            .forEach(b -> b.getPersistent(MarkerId.WATERED_CROP.key, WateredCrop.class, WateredCrop::new));
    }
}
