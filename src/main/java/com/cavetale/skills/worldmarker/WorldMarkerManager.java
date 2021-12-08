package com.cavetale.skills.worldmarker;

import com.cavetale.skills.Farming;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.util.Effects;
import com.cavetale.worldmarker.block.BlockMarker;
import com.cavetale.worldmarker.block.BlockMarkerHook;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

@RequiredArgsConstructor
public final class WorldMarkerManager implements BlockMarkerHook {
    final SkillsPlugin plugin;
    Map<Block, WateredCrop> cropsMap = new HashMap<>();

    public void enable() {
        BlockMarker.registerHook(plugin, this);
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void disable() {
        for (WateredCrop wateredCrop : cropsMap.values()) {
            wateredCrop.disable();
        }
        cropsMap.clear();
    }

    @Override
    public void onBlockLoad(Block block, String id) {
        switch (id) {
        case Farming.WATERED_CROP: {
            if (Farming.Crop.of(block) == null) {
                BlockMarker.resetId(block);
                return;
            }
            WateredCrop wateredCrop = new WateredCrop(plugin, block);
            cropsMap.put(block, wateredCrop);
            wateredCrop.enable();
            return;
        }
        case Farming.GROWN_CROP:
            if (Farming.Crop.of(block) == null) {
                BlockMarker.resetId(block);
            }
            break;
        default: break;
        }
    }

    @Override
    public void onBlockUnload(Block block, String id) {
        switch (id) {
        case Farming.WATERED_CROP:
            WateredCrop wateredCrop = cropsMap.remove(block);
            if (wateredCrop != null) {
                wateredCrop.disable();
            }
            break;
        default: break;
        }
    }

    public WateredCrop getWateredCrop(Block block) {
        return cropsMap.get(block);
    }

    @Override
    public void onBlockSet(Block block, String id) {
        WateredCrop old = cropsMap.remove(block);
        if (old != null) old.disable();
        switch (id) {
        case Farming.WATERED_CROP: {
            WateredCrop wateredCrop = new WateredCrop(plugin, block);
            cropsMap.put(block, wateredCrop);
            wateredCrop.enable();
            break;
        }
        default:
            break;
        }
    }

    @Override
    public void onBlockReset(Block block) {
        WateredCrop wateredCrop = cropsMap.remove(block);
        if (wateredCrop != null) wateredCrop.disable();
    }

    void tick() {
        List<WateredCrop> crops = new ArrayList<>(cropsMap.values());
        Collections.shuffle(crops);
        long now = System.currentTimeMillis();
        final int max = 10;
        int count = 0;
        for (int i = 0; i < crops.size(); i += 1) {
            WateredCrop wc = crops.get(i);
            if (now < wc.particleCooldown) continue;
            wc.particleCooldown = now + 5000L;
            Effects.wateredCropAmbient(wc.block);
            count += 1;
            if (count > max) break;
        }
    }
}
