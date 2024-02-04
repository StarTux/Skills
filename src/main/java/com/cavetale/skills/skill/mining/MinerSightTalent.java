package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class MinerSightTalent extends Talent implements Listener {
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        final Block block = event.getBlock();
        if (!MiningSkill.anyStone(block) && !MiningSkill.metalOre(block) && !MiningSkill.gemOre(block)) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !MaterialTags.PICKAXES.isTagged(item.getType())) return;
        Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> {
                if (block.isEmpty()) {
                    player.sendBlockChange(block.getLocation(), Material.LIGHT.createBlockData());
                } else if (block.getType() == Material.WATER) {
                    final BlockData blockData = Material.LIGHT.createBlockData();
                    if (!(blockData instanceof Waterlogged waterlogged)) return;
                    waterlogged.setWaterlogged(true);
                    player.sendBlockChange(block.getLocation(), blockData);
                }
            }, 6L);
    }
}
