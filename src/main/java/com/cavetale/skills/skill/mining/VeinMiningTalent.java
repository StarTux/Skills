package com.cavetale.skills.skill.mining;

import com.cavetale.core.event.block.PlayerBlockAbilityQuery;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import static com.cavetale.core.exploits.PlayerPlacedBlocks.isPlayerPlaced;
import static com.cavetale.skills.SkillsPlugin.miningSkill;
import static com.cavetale.skills.SkillsPlugin.random;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.sessions;

public final class VeinMiningTalent extends Talent {
    protected VeinMiningTalent() {
        super(TalentType.VEIN_MINING);
    }

    @Override
    public String getDisplayName() {
        return "Vein Mining";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Mining rocky ores will attempt to break the entire vein",
                       "Works on :coal_ore:coal, :redstone_ore:redstone and :lapis_ore:lapis lazuli ores."
                       + " Requires the Efficiency enchantment on your pickaxe."
                       + " Each level of Efficiency lets you break 4 blocks at once."
                       + "\n\nMine without this feature by sneaking.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.IRON_PICKAXE);
    }

    /**
     * Called by MiningSkill.
     * Guaranteed:
     * - Player is in the right mode
     * - Item is pickaxe
     * - Block has reward: Reward is not null
     */
    protected boolean tryToVeinMine(Player player, ItemStack item, Block block, MiningReward reward, BlockBreakEvent event) {
        if (!isPlayerEnabled(player)) return false;
        if (player.isSneaking()) return false;
        final int efficiency = item.getEnchantmentLevel(Enchantment.DIG_SPEED);
        if (efficiency == 0) return false;
        Session session = sessions().of(player);
        if (!session.isTalentEnabled(TalentType.VEIN_METALS) && MiningSkill.metalOre(block)) return false;
        if (!session.isTalentEnabled(TalentType.VEIN_GEMS) && MiningSkill.gemOre(block)) return false;
        List<Block> vein = findVein(player, block, item, reward, efficiency);
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " vein=" + vein.size());
        }
        if (vein.size() < 2) return false;
        if (!(item.getItemMeta() instanceof Damageable itemMeta)) return false;
        final int unbreaking = itemMeta.getEnchantLevel(Enchantment.DURABILITY);
        int brokenBlockCount = 0;
        int rewardableBlockCount = 0;
        int itemDamageCount = 0;
        for (Block v : vein) {
            if (itemMeta.getDamage() >= item.getType().getMaxDurability()) break;
            if (!isPlayerPlaced(v)) {
                rewardableBlockCount += 1;
            }
            if (!miningSkill().mineMagnetTalent.breakBlock(player, item, v)) continue;
            brokenBlockCount += 1;
            // Item Damage
            double durabilityChance = 1.0 / (double) (unbreaking + 1);
            if (random().nextDouble() < durabilityChance) {
                itemMeta.setDamage(itemMeta.getDamage() + 1);
                itemDamageCount += 1;
            }
        }
        if (brokenBlockCount == 0) return false;
        if (itemDamageCount > 0) item.setItemMeta(itemMeta);
        event.setCancelled(true);
        if (rewardableBlockCount > 0) {
            miningSkill().giveStackedReward(player, item, reward, block.getLocation().add(0.5, 0.25, 0.5), rewardableBlockCount);
            miningSkill().rubyTalent.onVeinMine(player, block, reward, rewardableBlockCount);
        }
        return true;
    }

    private List<Block> findVein(Player player, Block originalBlock, ItemStack item, MiningReward reward, final int efficiency) {
        Material mat = reward.material;
        HashSet<Block> done = new HashSet<>();
        ArrayList<Block> vein = new ArrayList<>();
        done.add(originalBlock);
        vein.add(originalBlock);
        final int total = efficiency * 4;
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
