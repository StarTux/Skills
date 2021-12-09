package com.cavetale.skills.skill.mining;

import com.cavetale.skills.Util;
import lombok.RequiredArgsConstructor;
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
final class MiningListener implements Listener {
    protected final MiningSkill miningSkill;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem()) return;
        final ItemStack item = Util.getHand(player, event.getHand());
        final Block block = event.getClickedBlock();
        if (event.getHand() == EquipmentSlot.HAND && MiningSkill.isPickaxe(item)) {
            if (miningSkill.usePickaxe(player, block, event.getBlockFace(), item)) {
                event.setCancelled(true);
            }
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        miningSkill.mine(player, event.getBlock());
    }
}
