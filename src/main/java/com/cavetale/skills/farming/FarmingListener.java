package com.cavetale.skills.farming;

import com.cavetale.skills.Effects;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.util.Util;
import com.cavetale.skills.worldmarker.MarkerId;
import com.cavetale.worldmarker.block.BlockMarker;
import com.destroystokyo.paper.MaterialTags;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public final class FarmingListener implements Listener {
    private final SkillsPlugin plugin;
    private final FarmingSkill farming;

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.FARMLAND) {
            ItemStack item = event.getItemInHand();
            if (MaterialTags.HOES.isTagged(item.getType())) {
                Effects.hoe(block, event.getBlockReplacedState().getBlockData());
            }
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem()) return;
        // Item
        final ItemStack item = Util.getHand(player, event.getHand());
        final Block block = event.getClickedBlock();
        if (item.getType() == Material.STICK) {
            if (farming.useStick(player, block)) {
                event.setCancelled(true);
            }
            return;
        }
        CropType crop = CropType.ofSeed(item);
        if (crop != null) {
            // Cancelling with edibles may trigger infinite eating animation.
            farming.useSeed(player, block, crop, item);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    void onBlockGrow(BlockGrowEvent event) {
        if (BlockMarker.hasId(event.getBlock(), MarkerId.WATERED_CROP)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (BlockMarker.hasId(block, MarkerId.WATERED_CROP)) {
            plugin.getWorldMarkerManager().removeWateredCrop(block);
            farming.onHarvestWateredCrop(event.getPlayer(), block);
        }
    }
}
