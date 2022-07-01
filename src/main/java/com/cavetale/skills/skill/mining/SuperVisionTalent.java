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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class SuperVisionTalent extends Talent implements Listener {
    protected final MiningSkill miningSkill;
    protected final BlockData fakeStoneData = Material.BLACK_STAINED_GLASS.createBlockData();
    protected final BlockData fakeDirtData = Material.WHITE_STAINED_GLASS.createBlockData();

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
        Session session = plugin.sessions.of(player);
        if (session.isSuperVisionActive()) return;
        Block block = event.getBlock();
        if (!MiningSkill.stone(block)) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        if (!MaterialTags.PICKAXES.isTagged(item.getType())) return;
        final int fortune = item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        if (fortune == 0) return;
        if (player.isSneaking()) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
                xray(player, block);
            });
    }

    /**
     * Called by scheduler.
     */
    protected int xray(@NonNull Player player, @NonNull Block block) {
        if (!player.isValid()) return 0;
        if (!player.getWorld().equals(block.getWorld())) return 0;
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return 0;
        // Actual XRay
        if (session.isSuperVisionActive()) return 0;
        session.setSuperVisionActive(true);
        final int radius = 3;
        final int realRadius = 2;
        final ArrayList<Block> bs = new ArrayList<>();
        final ArrayList<Block> br = new ArrayList<>();
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
                    if ((!MiningSkill.stone(nbor) && !MiningSkill.dirt(nbor)) || d > realRadius) {
                        br.add(nbor);
                    } else {
                        bs.add(nbor);
                    }
                }
            }
        }
        if (bs.isEmpty()) return 0;
        for (Block b : bs) {
            if (MiningSkill.dirt(b)) {
                fakeBlock(player, b, fakeDirtData);
            } else {
                fakeBlock(player, b, fakeStoneData);
            }
        }
        for (Block b : br) {
            fakeBlock(player, b, b.getBlockData());
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isValid()) return;
                plugin.sessions.apply(player, s -> s.setSuperVisionActive(false));
                if (!player.getWorld().equals(block.getWorld())) return;
                for (Block b : bs) {
                    if (!player.isValid()) return;
                    if (!player.getWorld().equals(block.getWorld())) return;
                    fakeBlock(player, b, b.getBlockData());
                }
            }, 60L); // 3 seconds
        return bs.size();
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
