package com.cavetale.skills.skill.mining;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.bukkit.Bukkit;
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
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.sessions;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class NetherVisionTalent extends Talent implements Listener {
    protected static final BlockData GLASS = Material.RED_STAINED_GLASS.createBlockData();

    protected NetherVisionTalent() {
        super(TalentType.NETHER_VISION);
    }

    @Override
    public String getDisplayName() {
        return "Nether Vision";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Mining nether stone with a Fortune pickaxe"
                       + " allows you to see through nether stones",
                       "Nearby nether stone will be rendered see-through"
                       + " for a few seconds so you can identify ores more easily."
                       + " Nether stones include: Netherrack, Basalt, Blackstone");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.RED_STAINED_GLASS);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        Session session = sessionOf(player);
        if (session.isNetherVisionActive()) return;
        Block block = event.getBlock();
        if (!MiningSkill.netherStone(block)) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        if (!MaterialTags.PICKAXES.isTagged(item.getType())) return;
        final int fortune = item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        if (fortune == 0) return;
        Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                xray(player, block);
            });
    }

    /**
     * Called by scheduler.
     */
    protected int xray(@NonNull Player player, @NonNull Block block) {
        if (!player.isValid()) return 0;
        if (!player.getWorld().equals(block.getWorld())) return 0;
        Session session = sessionOf(player);
        if (!session.isEnabled()) return 0;
        // Actual XRay
        if (session.isNetherVisionActive()) return 0;
        session.setNetherVisionActive(true);
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
                    if ((!MiningSkill.netherStone(nbor) && !MiningSkill.netherDirt(nbor)) || d > realRadius) {
                        br.add(nbor);
                    } else {
                        bs.add(nbor);
                    }
                }
            }
        }
        if (bs.isEmpty()) return 0;
        for (Block b : bs) {
            fakeBlock(player, b, GLASS);
        }
        for (Block b : br) {
            fakeBlock(player, b, b.getBlockData());
        }
        Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> {
                if (!player.isValid()) return;
                sessions().apply(player, s -> s.setNetherVisionActive(false));
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
