package com.cavetale.skills.worldmarker;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.worldmarker.block.BlockMarker;
import com.cavetale.worldmarker.block.BlockMarkerHook;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;

@RequiredArgsConstructor
public final class WorldMarkerManager implements BlockMarkerHook {
    final SkillsPlugin plugin;
    Map<Block, WateredCrop> cropsMap = new HashMap<>();

    public void enable() {
        WateredCrop.init();
        BlockMarker.registerHook(plugin, this);
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
        case MarkerId.WATERED_CROP: {
            WateredCrop wateredCrop = new WateredCrop(block);
            cropsMap.put(block, wateredCrop);
            wateredCrop.load();
            wateredCrop.enable();
            break;
        }
        default: break;
        }
    }

    @Override
    public void onBlockUnload(Block block, String id) {
        switch (id) {
        case MarkerId.WATERED_CROP:
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

    public WateredCrop makeWateredCrop(Block block) {
        BlockMarker.setId(block, MarkerId.WATERED_CROP);
        WateredCrop wateredCrop = new WateredCrop(block);
        cropsMap.put(block, wateredCrop);
        wateredCrop.enable();
        return wateredCrop;
    }

    public void removeWateredCrop(Block block) {
        WateredCrop wateredCrop = cropsMap.remove(block);
        if (wateredCrop != null) {
            wateredCrop.disable();
            wateredCrop.remove();
        }
    }
}
