package com.cavetale.skills.mining;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.util.Util;
import com.cavetale.skills.worldmarker.MarkerId;
import com.cavetale.worldmarker.BlockMarker;
import com.destroystokyo.paper.MaterialTags;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public final class MiningListener implements Listener {
    final SkillsPlugin plugin;
    final MiningSkill mining;

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
        if (event.getHand() == EquipmentSlot.HAND && MaterialTags.PICKAXES.isTagged(item.getType())) {
            if (mining.usePickaxe(player, block, event.getBlockFace(), item)) {
                event.setCancelled(true);
            }
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        Block block = event.getBlock();
        String stringId = BlockMarker.getId(block);
        if (stringId != null) {
            MarkerId id = MarkerId.of(stringId);
            switch (id) {
            case WATERED_CROP:
                BlockMarker.resetId(block);
                break;
            default: break;
            }
        }
        mining.mine(player, block);
    }
}
