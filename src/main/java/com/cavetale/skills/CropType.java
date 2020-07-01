package com.cavetale.skills;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public enum CropType {
    // 8 grow stages (0-7)
    WHEAT(10, Material.WHEAT, Material.WHEAT, Material.WHEAT_SEEDS),
    CARROT(10, Material.CARROTS, Material.CARROT),
    POTATO(10,Material.POTATOES, Material.POTATO),
    // 4 grow stages (0-3)
    BEETROOT(5, Material.BEETROOTS, Material.BEETROOT, Material.BEETROOT_SEEDS),
    NETHER_WART(5, Material.NETHER_WART, Material.NETHER_WART);

    public final int points;
    public final Material blockMaterial;
    public final Material itemMaterial;
    public final Material seedMaterial;

    CropType(final int points, final Material blockMaterial, final Material itemMaterial, final Material seedMaterial) {
        this.points = points;
        this.blockMaterial = blockMaterial;
        this.itemMaterial = itemMaterial;
        this.seedMaterial = seedMaterial;
    }

    CropType(final int points, final Material blockMaterial, final Material itemMaterial) {
        this(points, blockMaterial, itemMaterial, itemMaterial);
    }

    static CropType of(Block block) {
        Material mat = block.getType();
        for (CropType type : CropType.values()) {
            if (type.blockMaterial == mat) return type;
        }
        return null;
    }

    static CropType ofSeed(ItemStack item) {
        Material mat = item.getType();
        for (CropType crop : CropType.values()) {
            if (crop.seedMaterial == mat) return crop;
        }
        return null;
    }
}

