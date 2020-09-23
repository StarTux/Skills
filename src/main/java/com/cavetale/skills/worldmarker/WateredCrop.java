package com.cavetale.skills.worldmarker;

import com.cavetale.skills.Effects;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.worldmarker.MarkBlock;
import com.cavetale.worldmarker.MarkTagContainer;
import com.cavetale.worldmarker.Persistent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

/**
 * This tag is stored in a block.
 */
@Getter
public final class WateredCrop implements Persistent {
    transient AreaEffectCloud aoeCloud = null;
    transient String coords = null;
    @Setter int water = 0;
    int growth = 0;
    static final int TICKS_PER_GROWTH = 20 * 60;

    @Override
    public SkillsPlugin getPlugin() {
        return SkillsPlugin.getInstance();
    }

    @Override
    public void onUnload(MarkTagContainer container) {
        if (aoeCloud != null) {
            aoeCloud.remove();
            aoeCloud = null;
        }
    }

    public static Ageable getAgeable(Block block) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Ageable)) return null;
        return (Ageable) blockData;
    }

    @Override
    public void onTickMarkBlock(MarkBlock markBlock) {
        Block block = markBlock.getBlock();
        Ageable ageable = getAgeable(block);
        if (ageable == null) {
            markBlock.resetId();
            return;
        }
        if (aoeCloud == null || !aoeCloud.isValid()) {
            aoeCloud = spawnAoeCloud(markBlock);
        } else {
            aoeCloud.setTicksLived(1);
        }
        if (water > 0) {
            water -= 1;
            boolean lit = block.getType() == Material.NETHER_WART
                ? true
                : block.getLightLevel() >= 10;
            if (lit) {
                growth += 1;
                if (growth >= TICKS_PER_GROWTH) {
                    growth = 0;
                    final int max = ageable.getMaximumAge();
                    final int age = ageable.getAge();
                    if (age < max) {
                        ageable.setAge(age + 1);
                        block.setBlockData(ageable, true);
                        Effects.cropGrow(block);
                    }
                }
            }
            updateAoeCloud(markBlock);
            markBlock.save();
        }
    }

    AreaEffectCloud spawnAoeCloud(MarkBlock markBlock) {
        if (coords == null) coords = markBlock.getCoordString();
        if (!markBlock.isBukkitChunkLoaded()) return null;
        Block block = markBlock.getBlock();
        return block.getWorld().spawn(block.getLocation().add(0.5, 0.25, 0.5), AreaEffectCloud.class, e -> {
                e.setBasePotionData(new PotionData(PotionType.AWKWARD));
                e.setParticle(Particle.WATER_WAKE);
                e.setPersistent(false);
                e.setRadius(0.5f);
                e.setDuration(10);
            });
    }

    public void updateAoeCloud(MarkBlock markBlock) {
        if (aoeCloud == null) {
            aoeCloud = spawnAoeCloud(markBlock);
        }
        if (aoeCloud != null) {
            if (water < 20 * 60) {
                aoeCloud.setParticle(Particle.WATER_WAKE);
            } else if (water < 20 * 60 * 10) {
                aoeCloud.setParticle(Particle.WATER_DROP);
            } else {
                aoeCloud.setParticle(Particle.WATER_SPLASH);
            }
        }
    }
}
