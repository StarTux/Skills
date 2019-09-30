package com.cavetale.skills;

import com.cavetale.worldmarker.BlockMarker;
import com.cavetale.worldmarker.MarkChunkTickEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
final class EventListener implements Listener {
    final SkillsPlugin plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem()) return;
        final ItemStack item = event.getItem();
        final Block block = event.getClickedBlock();
        if (item.getType() == Material.STICK) {
            plugin.growstick.use(event.getPlayer(), block);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    void onMarkChunkTick(MarkChunkTickEvent event) {
        event.getBlocksWithId(Growstick.WATERED_CROP)
            .forEach(plugin.growstick::tickWateredCrop);
        event.getBlocksWithId(Growstick.GROWN_CROP)
            .forEach(plugin.growstick::tickGrownCrop);
        plugin.combat.onTick(event.getChunk());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String bid = BlockMarker.getId(block);
        if (bid != null) {
            switch (bid) {
            case Growstick.WATERED_CROP:
                BlockMarker.resetId(block);
                break;
            case Growstick.GROWN_CROP:
                plugin.growstick.harvest(player, block);
                break;
            default: break;
            }
        }
        plugin.mining.mine(player, block);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    void onBlockGrow(BlockGrowEvent event) {
        if (BlockMarker.hasId(event.getBlock(), Growstick.WATERED_CROP)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    void onPlayerJoin(PlayerJoinEvent event) {
        plugin.loadSession(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerQuit(PlayerQuitEvent event) {
        plugin.removeSession(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer != null) {
            plugin.combat.kill(killer, entity);
        }
    }
}
