package com.cavetale.skills;

import com.cavetale.worldmarker.BlockMarker;
import com.cavetale.worldmarker.MarkBlock;
import com.winthier.exploits.Exploits;
import com.winthier.generic_events.GenericEvents;
import java.util.ArrayList;
import java.util.Collections;
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
 * Object to manage the Farming skill.
 * Called by EventListener et al, owned by SkillsPlugin.
 */
@RequiredArgsConstructor
final class Farming {
    final SkillsPlugin plugin;
    static final String WATERED_CROP = "skills:watered_crop";
    static final String GROWN_CROP = "skills:grown_crop";

    enum Crop {
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

        static Crop of(Block block) {
            Material mat = block.getType();
            for (Crop type : Crop.values()) {
                if (type.blockMaterial == mat) return type;
            }
            return null;
        }

        static Crop ofSeed(ItemStack item) {
            Material mat = item.getType();
            for (Crop crop : Crop.values()) {
                if (crop.seedMaterial == mat) return crop;
            }
            return null;
        }
    }

    /**
     * Player uses a growstick on a certain block.
     */
    boolean useStick(@NonNull Player player, @NonNull Block block) {
        if (Crop.of(block) == null && block.getType() != Material.FARMLAND) return false;
        int radius = 0;
        Session session = plugin.sessionOf(player);
        if (session.hasTalent(Talent.FARM_GROWSTICK_RADIUS)) radius = 1;
        boolean success = false;
        for (int dz = -radius; dz <= radius; dz += 1) {
            for (int dx = -radius; dx <= radius; dx += 1) {
                success |= waterBlock(player, block.getRelative(dx, 0, dz));
            }
        }
        if (success) Effects.wateringCan(player);
        return success;
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
        Block block = markBlock.getBlock();
        Crop crop = Crop.of(block);
        if (crop == null) {
            markBlock.resetId();
            return;
        }
        // Soil
        int ticks = markBlock.getTicksLoaded();
        Block soilBlock = block.getRelative(0, -1, 0);
        if (soilBlock.getType() == Material.FARMLAND) {
            waterSoil(soilBlock);
        }
        // Grow
        if (ticks > 0 && (ticks % 2400) == 0) {
            growCrop(markBlock, crop);
        }
        // Water Effect
        if (markBlock.getPlayerDistance() <= 1
            && (ticks % 10) == 0) {
            Effects.wateredCropAmbient(markBlock.getBlock());
        }
    }

    void tickGrownCrop(@NonNull MarkBlock markBlock) {
        if (Crop.of(markBlock.getBlock()) == null) {
            markBlock.resetId();
            return;
        }
        if (plugin.random.nextInt(20) == 0) {
            Effects.grownCropAmbient(markBlock.getBlock());
        }
    }

    void growCrop(@NonNull MarkBlock markBlock, @NonNull Crop crop) {
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
            if (crop != Crop.NETHER_WART) {
                if (block.getLightFromSky() < 1) return;
                long time = block.getWorld().getTime();
                if (time > 13000L && time < 23000L) return;
            }
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
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        Session session = plugin.sessionOf(player);
        // Extra crops
        if (session.hasTalent(Talent.FARM_CROP_DROPS)) {
            block.getWorld().dropItem(loc, new ItemStack(crop.itemMaterial,
                                                         plugin.random.nextInt(3) + 1));
        }
        // Special Rule
        if (crop == Crop.NETHER_WART || crop == Crop.BEETROOT) {
            if (plugin.random.nextBoolean()) return;
        }
        // Reward Diamond
        double gemChance = 0.01;
        final double roll = plugin.random.nextDouble();
        if (session.hasTalent(Talent.FARM_DIAMOND_DROPS)) gemChance = 0.02;
        if (roll < gemChance) {
            block.getWorld().dropItem(loc, new ItemStack(Material.DIAMOND));
            int inc = 1;
            if (session.hasTalent(Talent.FARM_TALENT_POINTS)) inc = 2;
            boolean noEffect = plugin.rollTalentPoint(player, inc);
            if (!noEffect) Effects.rewardJingle(loc);
        }
        // Exp
        plugin.addSkillPoints(player, SkillType.FARMING, 1);
        block.getWorld().spawn(loc, ExperienceOrb.class, orb -> orb.setExperience(1));
        Effects.harvest(block);
    }

    boolean useSeed(@NonNull Player player, @NonNull Block block,
                    @NonNull Crop crop, @NonNull ItemStack item) {
        Session session = plugin.sessionOf(player);
        Material soil = crop == Crop.NETHER_WART
            ? Material.SOUL_SAND
            : Material.FARMLAND;
        if (session.hasTalent(Talent.FARM_PLANT_RADIUS) && !player.isSneaking()) {
            if (block.getType() == soil) {
                return 0 < plantRadius(player, block.getRelative(0, 1, 0), crop, item);
            }
            Block lower = block.getRelative(0, -1, 0);
            if (lower.getType() == soil) {
                return 0 < plantRadius(player, block, crop, item);
            }
        }
        return false;
    }

    int plantRadius(@NonNull Player player, @NonNull Block orig,
                    @NonNull Crop crop, @NonNull ItemStack item) {
        int result = 0;
        ArrayList<Block> bs = new ArrayList<>(8);
        for (int z = -1; z <= 1; z += 1) {
            for (int x = -1; x <= 1; x += 1) {
                bs.add(orig.getRelative(x, 0, z));
            }
        }
        Collections.shuffle(bs, plugin.random);
        for (Block block : bs) {
            if (item.getType() != crop.seedMaterial) break;
            if (item.getAmount() < 1) break;
            if (!block.isEmpty()) continue;
            Block lower = block.getRelative(0, -1, 0);
            if (crop == Crop.NETHER_WART && lower.getType() != Material.SOUL_SAND) continue;
            if (crop != Crop.NETHER_WART && lower.getType() != Material.FARMLAND) continue;
            if (!GenericEvents.playerCanBuild(player, block)) continue;
            block.setType(crop.blockMaterial);
            Exploits.setPlayerPlaced(block, true);
            item.setAmount(item.getAmount() - 1);
            Effects.plantCropMagic(block);
            result += 1;
        }
        return result;
    }
}
