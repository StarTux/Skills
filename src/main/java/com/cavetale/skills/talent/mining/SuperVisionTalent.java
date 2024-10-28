package com.cavetale.skills.talent.mining;

import com.cavetale.core.struct.Vec3i;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class SuperVisionTalent extends Talent implements Listener {
    private static final BlockData FAKE_GLASS = Material.TINTED_GLASS.createBlockData();
    private final Set<Material> superVisionMaterials = new HashSet<>();

    public SuperVisionTalent() {
        super(TalentType.SUPER_VISION, "Super Vision",
              "Mining stone allows you to see through solid stone.",
              "Nearby stone will be rendered see-through for a few seconds so you can identify ores more easily.");
        addLevel(1, "Super Vision radius " + levelToRadius(1));
        addLevel(1, "Super Vision radius " + levelToRadius(2));
        addLevel(1, "Super Vision radius " + levelToRadius(3));
        addLevel(1, "Super Vision radius " + levelToRadius(4));
        addLevel(1, "Super Vision radius " + levelToRadius(5));
        // Materials
        superVisionMaterials.add(Material.STONE);
        superVisionMaterials.add(Material.DIORITE);
        superVisionMaterials.add(Material.ANDESITE);
        superVisionMaterials.add(Material.GRANITE);
        superVisionMaterials.add(Material.DEEPSLATE);
        superVisionMaterials.add(Material.TUFF);
        superVisionMaterials.add(Material.GRAVEL);
        superVisionMaterials.add(Material.DIRT);
        superVisionMaterials.add(Material.NETHERRACK);
    }

    private static int levelToRadius(int level) {
        return level + 1;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.TINTED_GLASS);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (player.isSneaking()) return;
        if (!isPlayerEnabled(player)) return;
        if (!isSuperVisionBlock(event.getBlock())) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        if (!MaterialTags.PICKAXES.isTagged(item.getType())) return;
        Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                xray(player, event.getBlock());
            });
    }

    /**
     * Make fake blocks visible in case one of them is left clicked by
     * the player so they can break them with the correct speed.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    private void onBlockDamage(BlockDamageEvent event) {
        final Player player = event.getPlayer();
        final Session session = Session.of(player);
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
    public int xray(@NonNull Player player, @NonNull Block block) {
        if (!player.isValid()) return 0;
        if (!player.getWorld().equals(block.getWorld())) return 0;
        final Session session = Session.of(player);
        if (!session.isEnabled()) return 0;
        final Tag tag = getOrCreateTag(session);
        final int level = session.getTalentLevel(talentType);
        final int radius = levelToRadius(level);
        final int radiusPlusOne = radius + 1;
        // Send vision blocks
        final List<Block> visionBlocks = getVisionBlocks(player, radiusPlusOne);
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
        for (int y = -radiusPlusOne; y <= radiusPlusOne; y += 1) {
            for (int z = -radiusPlusOne; z <= radiusPlusOne; z += 1) {
                for (int x = -radiusPlusOne; x <= radiusPlusOne; x += 1) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block nbor = block.getRelative(x, y, z);
                    if (nbor.getY() < min) continue;
                    if (nbor.isEmpty() || nbor.isLiquid()) continue;
                    int d = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
                    if (visionBlocks.contains(nbor)) {
                        no.add(nbor);
                    } else if (d > radius) {
                        // Blocks right outside the visible area
                        if (tag.fakeBlockMap.containsKey(Vec3i.of(nbor))) {
                            continue;
                        }
                        no.add(nbor);
                    } else if (isSuperVisionBlock(nbor)) {
                        yes.add(nbor);
                    } else {
                        no.add(nbor);
                    }
                }
            }
        }
        if (yes.isEmpty()) return 0;
        for (Block b : yes) {
            sendFakeBlock(player, b, FAKE_GLASS);
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
        Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> cleanup(session), 1L);
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

    public void sendRealBlock(Player player, Block block) {
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

    private boolean isSuperVisionBlock(Block block) {
        return superVisionMaterials.contains(block.getType());
    }
}
