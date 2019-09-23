package com.cavetale.skills;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.block.Block;

enum WateredCropType {
    WHEAT(Material.WHEAT),
    ;

    public final Material blockMaterial;

    WateredCropType(@NonNull final Material blockMaterial) {
        this.blockMaterial = blockMaterial;
    }

    static WateredCropType of(Block block) {
        Material mat = block.getType();
        for (WateredCropType type : WateredCropType.values()) {
            if (type.blockMaterial == mat) return type;
        }
        return null;
    }
}
