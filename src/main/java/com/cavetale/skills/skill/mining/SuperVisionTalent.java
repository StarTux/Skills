package com.cavetale.skills.skill.mining;

import com.cavetale.core.struct.Vec3i;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class SuperVisionTalent extends Talent implements Listener {
    protected static final BlockData STONE_GLASS = Material.LIGHT_GRAY_STAINED_GLASS.createBlockData();
    protected static final BlockData DEEP_GLASS = Material.BLACK_STAINED_GLASS.createBlockData();

    protected SuperVisionTalent() {
        super(TalentType.SUPER_VISION);
    }

    @Override
    public String getDisplayName() {
        return "Super Vision";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Mining stone with a Fortune pickaxe"
                       + " allows you to see through solid stone",
                       "Nearby stone will be rendered see-through"
                       + " for a few seconds so you can identify ores more easily.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.LIGHT_GRAY_STAINED_GLASS);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        final boolean hasDeep = sessionOf(player).isTalentEnabled(TalentType.DEEP_VISION);
        if (!hasDeep) {
            if (!MiningSkill.stone(event.getBlock())) return;
        } else {
            if (!MiningSkill.anyStone(event.getBlock())) return;
        }
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        if (!MaterialTags.PICKAXES.isTagged(item.getType())) return;
        final int fortune = item.getEnchantmentLevel(Enchantment.FORTUNE);
        if (fortune == 0) return;
        if (player.isSneaking()) return;
        Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                xray(player, event.getBlock(), hasDeep);
            });
    }

    /**
     * Make fake blocks visible in case one of them is left clicked by
     * the player so they can break them with the correct speed.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    private void onBlockDamage(BlockDamageEvent event) {
        final Player player = event.getPlayer();
        final Session session = sessionOf(player);
        if (!session.isEnabled()) return;
        final Tag tag = session.getMining().getSuperVisionTag();
        if (tag == null || tag.fakeBlockMap.isEmpty()) return;
        final Block block = event.getBlock();
        if (tag.fakeBlockMap.remove(Vec3i.of(block)) != null) {
            sendRealBlock(player, block);
        }
        // Do vision blocks as well
        for (Block vision : getVisionBlocks(player, 2)) {
            if (tag.fakeBlockMap.remove(Vec3i.of(vision)) == null) continue;
            sendRealBlock(player, vision);
        }
    }

    /**
     * Turn stone blocks within a radius into glass.
     */
    protected int xray(@NonNull Player player, @NonNull Block block, final boolean hasDeep) {
        if (!player.isValid()) return 0;
        if (!player.getWorld().equals(block.getWorld())) return 0;
        final Session session = sessionOf(player);
        if (!session.isEnabled()) return 0;
        final Tag tag = getOrCreateTag(session);
        final int radius = 3;
        final int realRadius = 2;
        // Send vision blocks
        final List<Block> visionBlocks = getVisionBlocks(player, radius);
        for (var it : visionBlocks) {
            final var vec = Vec3i.of(it);
            if (tag.fakeBlockMap.remove(vec) == null) continue;
            sendRealBlock(player, block);
        }
        // Actual XRay
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
                    if (visionBlocks.contains(nbor)) {
                        no.add(nbor);
                    } else if (d > realRadius) {
                        // Blocks right outside the visible area
                        if (tag.fakeBlockMap.containsKey(Vec3i.of(nbor))) {
                            continue;
                        }
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
            if (MiningSkill.deepStone(b)) {
                sendFakeBlock(player, b, DEEP_GLASS);
            } else {
                sendFakeBlock(player, b, STONE_GLASS);
            }
            tag.fakeBlockMap.put(Vec3i.of(b), System.currentTimeMillis() + 5000L);
        }
        for (Block b : no) {
            sendRealBlock(player, b);
            tag.fakeBlockMap.remove(Vec3i.of(b));
        }
        scheduleCleanup(session);
        return yes.size();
    }

    private void scheduleCleanup(final Session session) {
        final Tag tag = getOrCreateTag(session);
        if (tag.cleanupScheduled) return;
        tag.cleanupScheduled = true;
        Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> cleanup(session), 20L);
    }

    private void cleanup(Session session) {
        if (!session.isEnabled()) return;
        final Player player = session.getPlayer();
        if (player == null) return;
        final Tag tag = getOrCreateTag(session);
        if (tag.fakeBlockMap.isEmpty()) return;
        tag.cleanupScheduled = false;
        final long now = System.currentTimeMillis();
        final Vec3i here = Vec3i.of(player.getLocation());
        final int dist = player.getWorld().getViewDistance() * 16;
        for (Iterator<Map.Entry<Vec3i, Long>> iter = tag.fakeBlockMap.entrySet().iterator(); iter.hasNext();) {
            final var entry = iter.next();
            final long expiry = entry.getValue();
            if (now < expiry) continue;
            iter.remove();
            final Vec3i vec = entry.getKey();
            if (here.maxHorizontalDistance(vec) > dist) continue;
            sendRealBlock(player, vec.toBlock(player.getWorld()));
        }
        if (tag.fakeBlockMap.isEmpty()) return;
        scheduleCleanup(session);
    }

    private void sendFakeBlock(Player player, Block block, BlockData fake) {
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

    protected void sendRealBlock(Player player, Block block) {
        player.sendBlockChange(block.getLocation(), block.getBlockData());
    }

    private Tag getOrCreateTag(Session session) {
        Tag tag = session.getMining().getSuperVisionTag();
        if (tag == null) {
            tag = new Tag();
            session.getMining().setSuperVisionTag(tag);
        }
        final Player player = session.getPlayer();
        if (player != null && !player.getWorld().getName().equals(tag.worldName)) {
            tag.fakeBlockMap.clear();
            tag.worldName = player.getWorld().getName();
        }
        return tag;
    }

    private static List<Block> getVisionBlocks(Player player, int distance) {
        final var result = new ArrayList<Block>();
        final var blockIterator = new BlockIterator(player, distance);
        while (blockIterator.hasNext()) {
            result.add(blockIterator.next());
        }
        return result;
    }

    public static final class Tag {
        private boolean cleanupScheduled;
        private String worldName = "";
        private Map<Vec3i, Long> fakeBlockMap = new HashMap<>();
    }
}
