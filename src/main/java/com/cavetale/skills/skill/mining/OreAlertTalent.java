package com.cavetale.skills.skill.mining;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import java.util.ArrayList;
import java.util.HashSet;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.random;

public final class OreAlertTalent extends Talent implements Listener {
    private final HashSet<Material> oreAlertMaterials = new HashSet<>();

    protected OreAlertTalent() {
        super(TalentType.ORE_ALERT, "Ore Alert",
              "Get alerts when a valuable :diamond_ore:ore is nearby",
              "Whenever you break stone with a pickaxe and there is :diamond_ore:Diamond Ore, :emerald_ore:Emerald Ore, or :ancient_debris:Ancient Debris nearby, an alert sound will notify you of its existence..");
        addLevel(1, "Ore Alert radius " + levelToRadius(1));
        addLevel(1, "Ore Alert radius " + levelToRadius(2));
        addLevel(1, "Ore Alert radius " + levelToRadius(3));
        // Materials
        oreAlertMaterials.add(Material.NETHERRACK);
        oreAlertMaterials.add(Material.STONE);
        oreAlertMaterials.add(Material.DIORITE);
        oreAlertMaterials.add(Material.ANDESITE);
        oreAlertMaterials.add(Material.GRANITE);
        oreAlertMaterials.add(Material.DEEPSLATE);
        oreAlertMaterials.add(Material.TUFF);
    }

    private static int levelToRadius(int level) {
        return level + 1;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.DIAMOND_ORE);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        Block block = event.getBlock();
        if (!oreAlertMaterials.contains(block.getType())) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !MaterialTags.PICKAXES.isTagged(item.getType())) return;
        oreAlert(player, block);
    }

    protected boolean oreAlert(@NonNull Player player, @NonNull Block block) {
        final int level = Session.of(player).getTalentLevel(talentType);
        final int radius = levelToRadius(level);
        final int radius2 = radius * radius;
        final ArrayList<Block> oreBlocks = new ArrayList<>();
        final int min = block.getWorld().getMinHeight();
        for (int y = -radius; y <= radius; y += 1) {
            for (int z = -radius; z <= radius; z += 1) {
                for (int x = -radius; x <= radius; x += 1) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    if (x * x + y * y + z * z > radius2) continue;
                    final Block nbor = block.getRelative(x, y, z);
                    if (nbor.getY() < min) continue;
                    final Material mat = nbor.getType();
                    if (Tag.DIAMOND_ORES.isTagged(mat)) {
                        oreBlocks.add(nbor);
                    } else if (Tag.EMERALD_ORES.isTagged(mat)) {
                        oreBlocks.add(nbor);
                    } else if (mat == Material.ANCIENT_DEBRIS) {
                        oreBlocks.add(nbor);
                    }
                }
            }
        }
        if (oreBlocks.isEmpty()) return false;
        final Block ore = oreBlocks.get(random().nextInt(oreBlocks.size()));
        player.playSound(block.getLocation().add(0.5, 0.5, 0.5),
                         Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.BLOCKS, 1.0f, 2.0f);
        return true;
    }
}
