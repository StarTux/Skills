package com.cavetale.skills.skill.mining;

import com.cavetale.core.event.block.PlayerBlockAbilityQuery;
import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import com.destroystokyo.paper.MaterialTags;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.exploits.PlayerPlacedBlocks.isPlayerPlaced;
import static com.cavetale.skills.SkillsPlugin.miningSkill;
import static com.cavetale.skills.SkillsPlugin.sessions;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class VeinMiningTalent extends Talent implements Listener {
    protected VeinMiningTalent() {
        super(TalentType.VEIN_MINING);
    }

    @Override
    public String getDisplayName() {
        return "Vein Mining - Basic";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Mining certain ores will attempt to break the entire vein",
                      "Works on Coal, Redstone and Lapis Lazuli Ores."
                      + "Requires the Efficiency enchantment on your pickaxe.",
                       "Mine without this feature by sneaking.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.IRON_PICKAXE);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        final Block block = event.getBlock();
        final boolean sneak = player.isSneaking();
        final boolean metal = MiningSkill.metalOre(block);
        final boolean gem = MiningSkill.gemOre(block);
        // Vein Mining
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !MaterialTags.PICKAXES.isTagged(item.getType())) return;
        final int efficiency = item.getEnchantmentLevel(Enchantment.DIG_SPEED);
        MiningReward reward = miningSkill().rewards.get(block.getType());
        Session session = sessions().of(player);
        if (!session.isTalentEnabled(TalentType.VEIN_METALS) && metal) return;
        if (!session.isTalentEnabled(TalentType.VEIN_GEMS) && gem) return;
        if (!sneak && efficiency > 0 && reward != null) {
            Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                    if (!player.isValid()) return;
                    if (!player.getWorld().equals(block.getWorld())) return;
                    int stacks = mineVein(player, block, item, reward, efficiency);
                    if (stacks > 0) {
                        miningSkill().giveStackedReward(player, block, reward, block.getLocation().add(0.5, 0.25, 0.5), stacks);
                    }
                });
        }
    }

    /**
     * Called by scheduler.
     *
     * @bugs Does NOT deal damage to the pickaxe.
     */
    protected int mineVein(@NonNull Player player,
                           @NonNull Block block,
                           @NonNull ItemStack item,
                           @NonNull MiningReward reward,
                           final int efficiency) {
        Material mat = reward.material;
        HashSet<Block> done = new HashSet<>();
        HashSet<Block> todo = new HashSet<>();
        ArrayList<Block> vein = new ArrayList<>();
        todo.add(block);
        done.add(block);
        int total = efficiency * 4;
        while (!todo.isEmpty() && vein.size() < total) {
            Block pivot = todo.iterator().next();
            todo.remove(pivot);
            for (int y = -1; y <= 1; y += 1) {
                for (int z = -1; z <= 1; z += 1) {
                    for (int x = -1; x <= 1; x += 1) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block nbor = pivot.getRelative(x, y, z);
                        if (done.contains(nbor)) continue;
                        done.add(nbor);
                        if (nbor.getType() != mat) continue;
                        if (!PlayerBlockAbilityQuery.Action.BUILD.query(player, nbor)) continue;
                        todo.add(nbor);
                        vein.add(nbor);
                    }
                }
            }
        }
        int rewardableBlockCount = 0;
        for (Block v : vein) {
            if (!isPlayerPlaced(v)) {
                rewardableBlockCount++;
            }
            Bukkit.getPluginManager().callEvent(new PlayerBreakBlockEvent(player, v));
            Effects.mineBlockMagic(v);
            v.breakNaturally(item);
        }
        return rewardableBlockCount;
    }
}
