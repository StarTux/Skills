package com.cavetale.skills.talent.mining;

import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.mytems.Mytems;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class MineMagnetTalent extends Talent implements Listener {
    private UUID magnetPlayer; // ItemSpawnEvent

    public MineMagnetTalent() {
        super(TalentType.MINE_MAGNET, "Mining Magnet",
              "Broken blocks will drop items right at your feet.");
        addLevel(1, "Absorb mined blocks");
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
    public boolean breakBlock(Player player, ItemStack item, Block block) {
        if (!new PlayerBreakBlockEvent(player, block, item).callEvent()) return false;
        player.spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5), 16, 0.25, 0.25, 0.25, 0.0, block.getBlockData());
        player.playSound(block.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0f, 1.5f);
        if (isPlayerEnabled(player)) {
            magnetPlayer = player.getUniqueId();
        }
        block.breakNaturally(item, true);
        magnetPlayer = null;
        return true;
    }

    /**
     * Respond to breakNaturally().
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onItemSpawn(ItemSpawnEvent event) {
        if (magnetPlayer == null) return;
        final Player player = Bukkit.getPlayer(magnetPlayer);
        if (player == null) return;
        Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                final Item item = event.getEntity();
                if (item == null || !item.isValid()) return;
                item.teleport(player.getLocation());
                item.setPickupDelay(0);
            });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (player == null || !isPlayerEnabled(player)) return;
        Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                for (Item item : event.getItems()) {
                    item.teleport(player.getLocation());
                    item.setPickupDelay(0);
                    item.setOwner(player.getUniqueId());
                }
            });
    }
}
