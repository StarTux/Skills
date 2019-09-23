package com.cavetale.skills;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
final class EventListener implements Listener {
    final SkillsPlugin plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem()) return;
        // Item
        final ItemStack item = event.getItem();
        if (item.getType() != Material.STICK) return;
        // Block
        final Block block = event.getClickedBlock();
        WateredCropType cropType = WateredCropType.of(block);
        if (cropType == null) return;
        plugin.growstick.use(event.getPlayer(), block);
    }
}
