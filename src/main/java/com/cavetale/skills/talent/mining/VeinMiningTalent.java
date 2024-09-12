package com.cavetale.skills.talent.mining;

import com.cavetale.core.event.block.PlayerBlockAbilityQuery;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.mining.MiningReward;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import static com.cavetale.core.exploits.PlayerPlacedBlocks.isPlayerPlaced;
import static com.cavetale.skills.SkillsPlugin.miningSkill;
import static com.cavetale.skills.SkillsPlugin.random;

public final class VeinMiningTalent extends Talent {
    public VeinMiningTalent() {
        super(TalentType.VEIN_MINING, "Vein Mining",
              "Mining rocky ores will attempt to break the entire vein.",
              "Works on :coal_ore:coal, :redstone_ore:redstone and :lapis_ore:lapis lazuli ores.",
              "Mine without this feature by sneaking.");
        addLevel(1, "Mine veins with up to " + levelToBlocks(1) + " blocks");
        addLevel(1, "Mine veins with up to " + levelToBlocks(2) + " blocks");
        addLevel(1, "Mine veins with up to " + levelToBlocks(3) + " blocks");
        addLevel(1, "Mine veins with up to " + levelToBlocks(4) + " blocks");
        addLevel(1, "Mine veins with up to " + levelToBlocks(5) + " blocks");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.IRON_PICKAXE);
    }

    private static int levelToBlocks(int level) {
        return level * 4;
    }

    /**
     * Called by MiningSkill.
     * Guaranteed:
     * - Player is in the right mode
     * - Item is pickaxe
     * - Block has reward: Reward is not null
     */
    public boolean tryToVeinMine(Player player, ItemStack item, Block block, MiningReward reward, BlockBreakEvent event) {
        if (!isPlayerEnabled(player)) return false;
        if (player.isSneaking()) return false;
        Session session = Session.of(player);
        final int level = session.getTalentLevel(talentType);
        List<Block> vein = findVein(player, block, item, reward, levelToBlocks(level));
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + " vein=" + reward.getMaterial() + " size=" + vein.size());
        }
        if (vein.size() < 2) return false;
        final ItemMeta meta = item.getItemMeta();
        final Damageable dmg = !meta.isUnbreakable() && meta instanceof Damageable damageable
            ? damageable
            : null;
        final int unbreaking = meta.getEnchantLevel(Enchantment.UNBREAKING);
        int brokenBlockCount = 0;
        int rewardableBlockCount = 0;
        int itemDamageCount = 0;
        for (Block v : vein) {
            if (dmg != null && dmg.getDamage() >= item.getType().getMaxDurability()) {
                break;
            }
            if (!isPlayerPlaced(v)) {
                rewardableBlockCount += 1;
            }
            miningSkill().getMinerSightTalent().onWillBreakBlock(player, v);
            if (!miningSkill().mineMagnetTalent.breakBlock(player, item, v)) {
                continue;
            }
            brokenBlockCount += 1;
            // Item Damage
            double durabilityChance = 1.0 / (double) (unbreaking + 1);
            if (random().nextDouble() < durabilityChance) {
                if (dmg != null) {
                    dmg.setDamage(dmg.getDamage() + 1);
                    itemDamageCount += 1;
                }
            }
        }
        if (brokenBlockCount == 0) return false;
        if (dmg != null && itemDamageCount > 0) {
            item.setItemMeta(dmg);
        }
        event.setCancelled(true);
        if (rewardableBlockCount > 0) {
            final Location dropLocation = session.isTalentEnabled(TalentType.MINE_MAGNET)
                ? player.getLocation()
                : block.getLocation().add(0.5, 0.25, 0.5);
            miningSkill().giveStackedReward(player, item, vein, reward, dropLocation, rewardableBlockCount);
        }
        return true;
    }

    private List<Block> findVein(Player player, Block originalBlock, ItemStack item, MiningReward reward, final int total) {
        final Material mat = reward.getMaterial();
        final HashSet<Block> done = new HashSet<>();
        final ArrayList<Block> vein = new ArrayList<>();
        done.add(originalBlock);
        vein.add(originalBlock);
        for (int veinIndex = 0; veinIndex < vein.size() && vein.size() < total; veinIndex += 1) {
            Block pivot = vein.get(veinIndex);
            List<Block> nbors = new ArrayList<>();
            for (int y = -1; y <= 1; y += 1) {
                for (int z = -1; z <= 1; z += 1) {
                    for (int x = -1; x <= 1; x += 1) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block nbor = pivot.getRelative(x, y, z);
                        if (done.contains(nbor)) continue;
                        done.add(nbor);
                        if (nbor.getType() != mat) continue;
                        if (!PlayerBlockAbilityQuery.Action.BUILD.query(player, nbor)) continue;
                        nbors.add(nbor);
                    }
                }
            }
            if (nbors.isEmpty()) continue;
            Collections.shuffle(nbors, random());
            for (Block nbor : nbors) {
                if (nbors.size() < total) {
                    vein.add(nbor);
                }
            }
        }
        return vein;
    }
}
