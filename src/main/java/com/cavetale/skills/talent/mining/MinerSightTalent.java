package com.cavetale.skills.talent.mining;

import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Tag;
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
    private final Set<Material> minerSightMaterials = new HashSet<>();

    public MinerSightTalent() {
        super(TalentType.MINER_SIGHT, "Miner Sight",
              "Mining stone with a pickaxe creates a temporary light source.",
              "Stone includes:" + " :stone:Stone, :andesite:Andesite, :diorite:Diorite, :granite:Granite, :tuff:tuff, :deepslate:Deepslate.");
        addLevel(1, "Create a light source");
        // Materials
        minerSightMaterials.add(Material.STONE);
        minerSightMaterials.add(Material.DIORITE);
        minerSightMaterials.add(Material.ANDESITE);
        minerSightMaterials.add(Material.GRANITE);
        minerSightMaterials.add(Material.DEEPSLATE);
        minerSightMaterials.add(Material.TUFF);
        minerSightMaterials.add(Material.COBBLESTONE);
        minerSightMaterials.add(Material.MOSSY_COBBLESTONE);
        minerSightMaterials.addAll(Tag.STONE_BRICKS.getValues());
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.LIGHT);
    }

    public void onWillBreakBlock(Player player, Block block) {
        if (!isPlayerEnabled(player)) return;
        if (!isMinerSightBlock(block) && miningSkill().getReward(block) == null) return;
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

    private boolean isMinerSightBlock(Block block) {
        return minerSightMaterials.contains(block.getType());
    }
}
