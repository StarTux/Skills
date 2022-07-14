package com.cavetale.skills.skill.mining;

import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.mytems.Mytems;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

public final class MineMagnetTalent extends Talent implements Listener {
    private Location dropLocation; // ItemSpawnEvent

    public MineMagnetTalent() {
        super(TalentType.MINE_MAGNET);
    }

    @Override
    public String getDisplayName() {
        return "Mining Magnet";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Broken blocks will drop items right at your feet");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.MAGNET);
    }

    /**
     * Break a block magically on behalf of player and tool.  Mining
     * talents should funnel their custom block breaks through here.
     * @return true if block was broken, false otherwise.
     */
    protected boolean breakBlock(Player player, ItemStack item, Block block) {
        if (!PlayerBreakBlockEvent.call(player, block)) return false;
        Effects.mineBlockMagic(block);
        if (isPlayerEnabled(player)) {
            dropLocation = player.getLocation();
        }
        block.breakNaturally(item, true);
        dropLocation = null;
        return true;
    }

    /**
     * Respond to breakNaturally().
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onItemSpawn(ItemSpawnEvent event) {
        if (dropLocation == null) return;
        event.getEntity().teleport(dropLocation);
        event.getEntity().setPickupDelay(0);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (player == null || !isPlayerEnabled(player)) return;
        for (Item item : event.getItems()) {
            item.teleport(player.getLocation());
            item.setPickupDelay(0);
            item.setOwner(player.getUniqueId());
        }
    }
}
