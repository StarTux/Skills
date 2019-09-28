package com.cavetale.skills;

import com.cavetale.worldmarker.BlockMarker;
import com.cavetale.worldmarker.MarkBlock;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Object to manage growstick and watered block related features.
 * Called by EventListener et al, owned by SkillsPlugin.
 */
@RequiredArgsConstructor
final class Growstick {
    final SkillsPlugin plugin;
    static final String WATERED_CROP = "skills:watered_crop";
    static final String GROWN_CROP = "skills:grown_crop";

    enum Crop {
        WHEAT(Material.WHEAT),
        CARROT(Material.CARROTS),
        POTATO(Material.POTATOES),
        BEETROOT(Material.BEETROOTS),
        NETHER_WART(Material.NETHER_WART),
        COCOA(Material.COCOA),
        SWEET_BERRY(Material.SWEET_BERRY_BUSH);

        public final Material blockMaterial;

        Crop(@NonNull final Material blockMaterial) {
            this.blockMaterial = blockMaterial;
        }

        static Crop of(Block block) {
            Material mat = block.getType();
            for (Crop type : Crop.values()) {
                if (type.blockMaterial == mat) return type;
            }
            return null;
        }
    }

    /**
     * Player uses a growstick on a certain block.
     */
    void use(@NonNull Player player, @NonNull Block block) {
        if (Crop.of(block) == null && block.getType() != Material.FARMLAND) return;
        int radius = 1;
        for (int dz = -radius; dz <= radius; dz += 1) {
            for (int dx = -radius; dx <= radius; dx += 1) {
                waterBlock(player, block.getRelative(dx, 0, dz));
            }
        }
    }

    void waterBlock(@NonNull Player player, @NonNull Block block) {
        if (block.getType() == Material.FARMLAND) {
            waterSoil(block);
            waterCrop(player, block.getRelative(0, 1, 0));
            Effects.waterBlock(block);
            return;
        }
        waterCrop(player, block);
    }

    /**
     * Attempt to water the block. Do nothing if it's not a crop, is
     * ripe, already watered, or has another block id.
     *
     * Play the effect and set the id otherwise.
     */
    void waterCrop(@NonNull Player player, @NonNull Block block) {
        if (Crop.of(block) == null) return;
        if (isRipe(block)) return;
        if (BlockMarker.hasId(block)) return;
        BlockMarker.setId(block, WATERED_CROP);
        Effects.waterBlock(block);
    }

    void waterSoil(@NonNull Block block) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Farmland)) return;
        Farmland farmland = (Farmland) blockData;
        int max = farmland.getMaximumMoisture();
        if (farmland.getMoisture() != max) {
            farmland.setMoisture(max);
            block.setBlockData(farmland);
        }
    }

    void tickWateredCrop(@NonNull MarkBlock markBlock) {
        if (markBlock.getPlayerDistance() > 4) return;
        // Soil
        int ticks = markBlock.getTicksLoaded();
        Block soilBlock = markBlock.getBlock().getRelative(0, -1, 0);
        if (soilBlock.getType() == Material.FARMLAND) {
            waterSoil(soilBlock);
        }
        // Grow
        if (ticks > 0 && (ticks % 600) == 0) {
            growCrop(markBlock);
        }
        // Water Effect
        if (markBlock.getPlayerDistance() <= 1
            && (ticks % 10) == 0) {
            Effects.wateredBlockAmbient(markBlock.getBlock());
        }
    }

    void tickGrownCrop(@NonNull MarkBlock markBlock) {
        if (Crop.of(markBlock.getBlock()) == null) {
            markBlock.resetId();
            return;
        }
    }

    void growCrop(@NonNull MarkBlock markBlock) {
        Block block = markBlock.getBlock();
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Ageable) {
            Ageable ageable = (Ageable) blockData;
            int age = ageable.getAge();
            int max = ageable.getMaximumAge();
            if (age >= max) {
                markBlock.setId(GROWN_CROP);
                return;
            } else {
                ageable.setAge(age + 1);
            }
            block.setBlockData(blockData);
            Effects.cropGrow(block);
            if (age + 1 >= max) {
                markBlock.setId(GROWN_CROP);
            }
        } else {
            markBlock.resetId();
        }
    }

    boolean isRipe(@NonNull Block block) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Ageable)) return false;
        Ageable ageable = (Ageable) blockData;
        return ageable.getAge() >= ageable.getMaximumAge();
    }

    void harvest(@NonNull Player player, @NonNull Block block) {
        BlockMarker.resetId(block);
        Crop crop = Crop.of(block);
        if (crop == null) return;
        if (!isRipe(block)) return;
        // Reward diamond
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        if (plugin.random.nextDouble() < 0.01) {
            block.getWorld().dropItem(loc, new ItemStack(Material.DIAMOND));
            Effects.rewardJingle(loc);
        }
        // Exp
        block.getWorld().spawn(loc, ExperienceOrb.class, orb -> orb.setExperience(1));
    }
}
