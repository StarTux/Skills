package com.cavetale.skills;

import com.cavetale.skills.worldmarker.MarkerId;
import com.cavetale.skills.worldmarker.WateredCrop;
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
public final class Farming {
    final SkillsPlugin plugin;

    boolean isHoe(ItemStack item) {
        if (item == null) return false;
        switch (item.getType()) {
        case AIR:
            return false;
        case DIAMOND_HOE:
        case IRON_HOE:
        case STONE_HOE:
        case WOODEN_HOE:
        case GOLDEN_HOE:
            return true;
        default:
            return false;
        }
    }

    /**
     * Player uses a growstick on a certain block.
     */
    boolean useStick(@NonNull Player player, @NonNull Block block) {
        if (CropType.of(block) == null && block.getType() != Material.FARMLAND) return false;
        int radius = 0;
        Session session = plugin.sessions.of(player);
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
            boolean a = waterSoil(lower);
            boolean b = waterCrop(player, block);
            if (a || b) {
                Effects.waterBlock(block);
                return true;
            }
        }
        return false;
    }

    /**
     * Attempt to water the block. Do nothing if it's not a crop, is
     * ripe, or has another block id.
     *
     * Play the effect and set the id otherwise.
     */
    boolean waterCrop(@NonNull Player player, @NonNull Block block) {
        if (CropType.of(block) == null) return false;
        if (isRipe(block)) return false;
        MarkBlock markBlock = BlockMarker.getBlock(block);
        if (BlockMarker.hasId(block)) {
            if (!markBlock.hasId(MarkerId.WATERED_CROP.key)) return false;
        } else {
            markBlock.setId(MarkerId.WATERED_CROP.key);
        }
        WateredCrop wateredCrop = markBlock.getPersistent(plugin, MarkerId.WATERED_CROP.key, WateredCrop.class, WateredCrop::new);
        wateredCrop.setWater(24000); // One MC day?
        wateredCrop.updateAoeCloud(markBlock);
        markBlock.save();
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

    // void tickWateredCrop(@NonNull MarkBlock markBlock) {
    //     if (markBlock.getPlayerDistance() > 4) return;
    //     Block block = markBlock.getBlock();
    //     Crop crop = Crop.of(block);
    //     if (crop == null) {
    //         markBlock.resetId();
    //         return;
    //     }
    //     // Soil
    //     int ticks = markBlock.getTicksLoaded();
    //     Block soilBlock = block.getRelative(0, -1, 0);
    //     if (soilBlock.getType() == Material.FARMLAND) {
    //         waterSoil(soilBlock);
    //     }
    //     // Grow
    //     if (ticks > 0 && (ticks % 2400) == 0) {
    //         growCrop(markBlock, crop);
    //     }
    // }

    void tickGrownCrop(@NonNull MarkBlock markBlock) {
        if (CropType.of(markBlock.getBlock()) == null) {
            markBlock.resetId();
            return;
        }
    }

    boolean isRipe(@NonNull Block block) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Ageable)) return false;
        Ageable ageable = (Ageable) blockData;
        return ageable.getAge() >= ageable.getMaximumAge();
    }

    void onHarvest(@NonNull Player player, @NonNull Block block) {
        BlockMarker.resetId(block);
        CropType crop = CropType.of(block);
        if (crop == null) return;
        if (!isRipe(block)) return;
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        Session session = plugin.sessions.of(player);
        // Extra crops
        if (session.hasTalent(Talent.FARM_CROP_DROPS)) {
            block.getWorld().dropItem(loc, new ItemStack(crop.itemMaterial,
                                                         plugin.random.nextInt(3) + 1));
        }
        // Reward Diamond
        Block ore = block.getRelative(0, -1, 0);
        if (ore.getType() == Material.IRON_ORE && session.hasTalent(Talent.FARM_IRON_GROWTH)) {
            if (plugin.random.nextDouble() < 0.01) {
                block.getWorld().dropItem(loc, new ItemStack(Material.IRON_INGOT));
            }
        } else if (ore.getType() == Material.GOLD_ORE && session.hasTalent(Talent.FARM_GOLD_GROWTH)) {
            if (plugin.random.nextDouble() < 0.01) {
                block.getWorld().dropItem(loc, new ItemStack(Material.GOLD_INGOT));
            }
        } else if (ore.getType() == Material.DIAMOND_ORE && session.hasTalent(Talent.FARM_DIAMOND_GROWTH)) {
            if (plugin.random.nextDouble() < 0.01) {
                block.getWorld().dropItem(loc, new ItemStack(Material.DIAMOND));
            }
        }
        // Exp
        plugin.points.give(player, SkillType.FARMING, crop.points);
        block.getWorld().spawn(loc, ExperienceOrb.class, orb -> orb.setExperience(1));
        Effects.harvest(block);
    }

    boolean useSeed(@NonNull Player player, @NonNull Block block,
                    @NonNull CropType crop, @NonNull ItemStack item) {
        Session session = plugin.sessions.of(player);
        Material soil = crop == CropType.NETHER_WART
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
                    @NonNull CropType crop, @NonNull ItemStack item) {
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
            if (crop == CropType.NETHER_WART && lower.getType() != Material.SOUL_SAND) continue;
            if (crop != CropType.NETHER_WART && lower.getType() != Material.FARMLAND) continue;
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
