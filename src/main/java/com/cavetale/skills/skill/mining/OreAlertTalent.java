package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import com.destroystokyo.paper.MaterialTags;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.bukkit.Material;
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
    protected OreAlertTalent() {
        super(TalentType.ORE_ALERT);
    }

    @Override
    public String getDisplayName() {
        return "Diamond Ore Alert";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Get alerts when Diamond Ore is nearby",
                       "Whenever you break stone with a pickaxe and there is"
                       + " Diamond Ore nearby,"
                       + " an alert sound will notify you of its existence."
                       + " Follow that lead to earn more Diamonds.");
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
        if (!MiningSkill.anyStone(block)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !MaterialTags.PICKAXES.isTagged(item.getType())) return;
        oreAlert(player, block);
    }

    protected boolean oreAlert(@NonNull Player player, @NonNull Block block) {
        final int radius = 3;
        ArrayList<Block> bs = new ArrayList<>();
        final int min = block.getWorld().getMinHeight();
        for (int y = -radius; y <= radius; y += 1) {
            for (int z = -radius; z <= radius; z += 1) {
                for (int x = -radius; x <= radius; x += 1) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block nbor = block.getRelative(x, y, z);
                    if (nbor.getY() < min) continue;
                    Material mat = nbor.getType();
                    if (Tag.DIAMOND_ORES.isTagged(mat)) {
                        bs.add(nbor);
                    }
                }
            }
        }
        if (bs.isEmpty()) return false;
        Block ore = bs.get(random().nextInt(bs.size()));
        Effects.oreAlert(ore);
        return true;
    }
}
