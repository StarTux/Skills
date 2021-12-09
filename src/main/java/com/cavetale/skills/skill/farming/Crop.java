package com.cavetale.skills.skill.farming;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public enum Crop {
    // 8 grow stages (0-7)
    WHEAT(Material.WHEAT, Material.WHEAT, Material.WHEAT_SEEDS),
    CARROT(Material.CARROTS, Material.CARROT),
    POTATO(Material.POTATOES, Material.POTATO),
    // 4 grow stages (0-3)
    BEETROOT(Material.BEETROOTS, Material.BEETROOT, Material.BEETROOT_SEEDS),
    NETHER_WART(Material.NETHER_WART, Material.NETHER_WART);

    public final Material blockMaterial;
    public final Material itemMaterial;
    public final Material seedMaterial;

    Crop(@NonNull final Material blockMaterial,
         @NonNull final Material itemMaterial,
         @NonNull final Material seedMaterial) {
        this.blockMaterial = blockMaterial;
        this.itemMaterial = itemMaterial;
        this.seedMaterial = seedMaterial;
    }

    Crop(@NonNull final Material blockMaterial,
         @NonNull final Material itemMaterial) {
        this(blockMaterial, itemMaterial, itemMaterial);
    }

    public static Crop of(Block block) {
        Material mat = block.getType();
        for (Crop type : Crop.values()) {
            if (type.blockMaterial == mat) return type;
        }
        return null;
    }

    public static Crop ofSeed(ItemStack item) {
        Material mat = item.getType();
        for (Crop crop : Crop.values()) {
            if (crop.seedMaterial == mat) return crop;
        }
        return null;
    }
}
