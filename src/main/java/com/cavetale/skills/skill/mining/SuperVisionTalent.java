package com.cavetale.skills.skill.mining;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.ArrayList;
import lombok.NonNull;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public final class SuperVisionTalent extends Talent implements Listener {
    protected final MiningSkill miningSkill;
    protected static final BlockData GLASS = Material.BLACK_STAINED_GLASS.createBlockData();

    protected SuperVisionTalent(final SkillsPlugin plugin, final MiningSkill miningSkill) {
        super(plugin, TalentType.SUPER_VISION);
        this.miningSkill = miningSkill;
    }

    @Override
    protected void enable() { }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        final boolean hasDeep = plugin.sessions.of(player).isTalentEnabled(TalentType.DEEP_VISION);
        if (!hasDeep) {
            if (!MiningSkill.stone(event.getBlock())) return;
        } else {
            if (!MiningSkill.anyStone(event.getBlock())) return;
        }
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        if (!MaterialTags.PICKAXES.isTagged(item.getType())) return;
        final int fortune = item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        if (fortune == 0) return;
        if (player.isSneaking()) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
                xray(player, event.getBlock(), hasDeep);
            });
    }

    /**
     * Called by scheduler.
     */
    protected int xray(@NonNull Player player, @NonNull Block block, final boolean hasDeep) {
        if (!player.isValid()) return 0;
        if (!player.getWorld().equals(block.getWorld())) return 0;
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return 0;
        // Actual XRay
        if (session.isSuperVisionActive()) return 0;
        session.setSuperVisionActive(true);
        final int radius = 3;
        final int realRadius = 2;
        final ArrayList<Block> yes = new ArrayList<>();
        final ArrayList<Block> no = new ArrayList<>();
        Location loc = player.getLocation();
        int px = loc.getBlockX();
        int pz = loc.getBlockZ();
        final int min = block.getWorld().getMinHeight();
        for (int y = -radius; y <= radius; y += 1) {
            for (int z = -radius; z <= radius; z += 1) {
                for (int x = -radius; x <= radius; x += 1) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block nbor = block.getRelative(x, y, z);
                    if (nbor.getY() < min) continue;
                    if (nbor.isEmpty() || nbor.isLiquid()) continue;
                    int d = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
                    if (d > realRadius) {
                        no.add(nbor);
                    } else if (MiningSkill.dirt(nbor)) {
                        yes.add(nbor);
                    } else if (!hasDeep && MiningSkill.stone(nbor)) {
                        yes.add(nbor);
                    } else if (hasDeep && MiningSkill.anyStone(nbor)) {
                        yes.add(nbor);
                    } else {
                        no.add(nbor);
                    }
                }
            }
        }
        if (yes.isEmpty()) return 0;
        for (Block b : yes) {
            fakeBlock(player, b, GLASS);
        }
        for (Block b : no) {
            fakeBlock(player, b, b.getBlockData());
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isValid()) return;
                plugin.sessions.apply(player, s -> s.setSuperVisionActive(false));
                if (!player.getWorld().equals(block.getWorld())) return;
                for (Block b : yes) {
                    if (!player.isValid()) return;
                    if (!player.getWorld().equals(block.getWorld())) return;
                    fakeBlock(player, b, b.getBlockData());
                }
            }, 60L); // 3 seconds
        return yes.size();
    }

    protected void fakeBlock(Player player, Block block, BlockData fake) {
        player.sendBlockChange(block.getLocation(), fake);
        // Find spectators
        for (Player p : player.getWorld().getPlayers()) {
            if (p.equals(player)) continue;
            if (p.getGameMode() != GameMode.SPECTATOR) continue;
            Entity t = p.getSpectatorTarget();
            if (t == null || !t.equals(player)) continue;
            p.sendBlockChange(block.getLocation(), fake);
        }
    }
}
