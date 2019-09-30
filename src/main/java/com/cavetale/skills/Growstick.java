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
        WHEAT(Material.WHEAT, Material.WHEAT),
        CARROT(Material.CARROTS, Material.CARROT),
        POTATO(Material.POTATOES, Material.POTATO),
        BEETROOT(Material.BEETROOTS, Material.BEETROOT),
        NETHER_WART(Material.NETHER_WART, Material.NETHER_WART),
        COCOA(Material.COCOA, Material.COCOA_BEANS);

        public final Material blockMaterial;
        public final Material itemMaterial;

        Crop(@NonNull final Material blockMaterial,
             @NonNull final Material itemMaterial) {
            this.blockMaterial = blockMaterial;
            this.itemMaterial = itemMaterial;
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
        boolean success = false;
        for (int dz = -radius; dz <= radius; dz += 1) {
            for (int dx = -radius; dx <= radius; dx += 1) {
                success |= waterBlock(player, block.getRelative(dx, 0, dz));
            }
        }
        if (success) Effects.wateringCan(player);
    }

    boolean waterBlock(@NonNull Player player, @NonNull Block block) {
        if (block.getType() == Material.FARMLAND) {
            Block upper = block.getRelative(0, 1, 0);
            if (waterSoil(block) || waterCrop(player, upper)) {
                Effects.waterBlock(upper);
                return true;
            }
        } else {
            Block lower = block.getRelative(0, -1, 0);
            if (waterSoil(lower) || waterCrop(player, block)) {
                Effects.waterBlock(block);
                return true;
            }
        }
        return false;
    }

    /**
     * Attempt to water the block. Do nothing if it's not a crop, is
     * ripe, already watered, or has another block id.
     *
     * Play the effect and set the id otherwise.
     */
    boolean waterCrop(@NonNull Player player, @NonNull Block block) {
        if (Crop.of(block) == null) return false;
        if (isRipe(block)) return false;
        if (BlockMarker.hasId(block)) return false;
        BlockMarker.setId(block, WATERED_CROP);
        return true;
    }

    boolean waterSoil(@NonNull Block block) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Farmland)) return false;
        Farmland farmland = (Farmland) blockData;
        int max = farmland.getMaximumMoisture();
        if (farmland.getMoisture() >= max) return false;
        farmland.setMoisture(max);
        block.setBlockData(farmland);
        return true;
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
        if ((markBlock.getTicksLoaded() % 40) == 0) {
            Effects.grownBlockAmbient(markBlock.getBlock());
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
            }
            if (block.getLightFromSky() < 1) return;
            long time = block.getWorld().getTime();
            if (time > 13000L && time < 23000L) return;
            ageable.setAge(age + 1);
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
        block.getWorld().dropItem(loc, new ItemStack(crop.itemMaterial,
                                                     plugin.random.nextInt(3) + 1));
        // Exp
        plugin.addSkillPoints(player, SkillType.FARMING, 1);
        block.getWorld().spawn(loc, ExperienceOrb.class, orb -> orb.setExperience(1));
        Effects.harvest(block);
    }
}
