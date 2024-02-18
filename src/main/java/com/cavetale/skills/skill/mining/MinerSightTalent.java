package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import static com.cavetale.skills.SkillsPlugin.miningSkill;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class MinerSightTalent extends Talent {
    protected MinerSightTalent() {
        super(TalentType.MINER_SIGHT);
    }

    @Override
    public String getDisplayName() {
        return "Miner Sight";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Mining stone with a pickaxe creates a light source",
                       "Stone includes:"
                       + " :stone:Stone,"
                       + " :andesite:Andesite,"
                       + " :diorite:Diorite,"
                       + " :granite:Granite,"
                       + " :tuff:tuff,"
                       + " :deepslate:Deepslate.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.LANTERN);
    }

    protected void onWillBreakBlock(Player player, Block block) {
        if (!isPlayerEnabled(player)) return;
        if (!MiningSkill.anyStone(block) && !MiningSkill.buildingStone(block) && miningSkill().getReward(block) == null) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !MaterialTags.PICKAXES.isTagged(item.getType())) return;
        final String worldName = player.getWorld().getName();
        final int distance = player.getWorld().getViewDistance() * 16;
        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                ticks += 1;
                if (ticks > 15) {
                    cancel();
                    return;
                }
                if (!player.isValid() || !player.isOnline() || !player.getWorld().getName().equals(worldName)) return;
                final var loc = player.getLocation();
                if (Math.abs(loc.getBlockX() - block.getX()) > distance) return;
                if (Math.abs(loc.getBlockZ() - block.getZ()) > distance) return;
                final BlockData blockData = Material.LIGHT.createBlockData();
                if (blockData instanceof Light light) light.setLevel(ticks);
                if (block.isEmpty()) {
                    player.sendBlockChange(block.getLocation(), blockData);
                } else if (block.getType() == Material.WATER) {
                    if (blockData instanceof Waterlogged waterlogged) waterlogged.setWaterlogged(true);
                    player.sendBlockChange(block.getLocation(), blockData);
                }
            }
        }.runTaskTimer(skillsPlugin(), 6L, 1L);
    }
}
