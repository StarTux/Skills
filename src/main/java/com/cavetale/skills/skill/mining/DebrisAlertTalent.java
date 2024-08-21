package com.cavetale.skills.skill.mining;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import com.destroystokyo.paper.MaterialTags;
import java.util.ArrayList;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.random;

public final class DebrisAlertTalent extends Talent implements Listener {
    protected DebrisAlertTalent() {
        super(TalentType.DEBRIS_ALERT,  "Ancient Debris Alert",
              "Get alerts when Ancient Debris is nearby",
              "Whenever you break nether stones with a pickaxe and there is Ancient Debris nearby, an alert sound will notify you of its existence. Follow that lead to earn more Ancient Debris. Nether stones include: Netherrack, Basalt, Blackstone");
        addLevel(5, "Alert when Ancient Debris is nearby");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.ANCIENT_DEBRIS);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        Block block = event.getBlock();
        if (!MiningSkill.netherStone(block)) return;
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
                    if (mat == Material.ANCIENT_DEBRIS) {
                        bs.add(nbor);
                    }
                }
            }
        }
        if (bs.isEmpty()) return false;
        Block ore = bs.get(random().nextInt(bs.size()));
        Effects.debrisAlert(ore);
        return true;
    }
}
