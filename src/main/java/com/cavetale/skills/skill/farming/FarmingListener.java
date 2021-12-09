package com.cavetale.skills.skill.farming;

import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.skills.Util;
import com.cavetale.skills.util.Effects;
import com.cavetale.worldmarker.block.BlockMarker;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
final class FarmingListener implements Listener {
    protected final FarmingSkill farmingSkill;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem()) return;
        final ItemStack item = Util.getHand(player, event.getHand());
        final Block block = event.getClickedBlock();
        if (item.getType() == Material.STICK) {
            if (farmingSkill.useStick(player, block)) {
                event.setCancelled(true);
            }
            return;
        }
        Crop crop = Crop.ofSeed(item);
        if (crop != null) {
            // Cancelling with edibles may trigger infinite eating animation.
            farmingSkill.useSeed(player, block, crop, item);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        Block block = event.getBlock();
        String bid = BlockMarker.getId(block);
        if (bid != null) {
            switch (bid) {
            case FarmingSkill.WATERED_CROP:
            case FarmingSkill.GROWN_CROP:
                BlockMarker.resetId(block);
                farmingSkill.harvest(player, block);
                break;
            default: break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onPlayerBreakBlock(PlayerBreakBlockEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        Block block = event.getBlock();
        String bid = BlockMarker.getId(block);
        if (bid != null) {
            switch (bid) {
            case FarmingSkill.WATERED_CROP:
            case FarmingSkill.GROWN_CROP:
                BlockMarker.resetId(block);
                farmingSkill.harvest(player, block);
                break;
            default: break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockFromTo(BlockFromToEvent event) {
        Block block = event.getBlock();
        String bid = BlockMarker.getId(block);
        if (bid != null) {
            switch (bid) {
            case FarmingSkill.WATERED_CROP:
            case FarmingSkill.GROWN_CROP:
                BlockMarker.resetId(block);
            default: break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    protected void onBlockGrow(BlockGrowEvent event) {
        if (BlockMarker.hasId(event.getBlock(), FarmingSkill.WATERED_CROP)) {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    protected void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack item = event.getItemInHand();
        if (farmingSkill.isHoe(item)) {
            if (block.getType() == Material.FARMLAND) {
                Effects.hoe(block, event.getBlockReplacedState().getBlockData());
            }
        }
    }
}
