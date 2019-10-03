package com.cavetale.skills;

import com.winthier.exploits.Exploits;
import com.winthier.generic_events.GenericEvents;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import lombok.NonNull;
import lombok.Value;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

final class Mining {
    final SkillsPlugin plugin;
    final EnumMap<Material, Reward> rewards = new EnumMap<>(Material.class);

    @Value
    static class Reward {
        final Material material;
        final int sp;
        final int exp;
    }

    private void reward(@NonNull Material material, final int sp, final int exp) {
        rewards.put(material, new Reward(material, sp, exp));
    }

    Mining(@NonNull final SkillsPlugin plugin) {
        this.plugin = plugin;
        reward(Material.DIAMOND_ORE,       10, 0);
        reward(Material.EMERALD_ORE,       5, 0);
        reward(Material.IRON_ORE,          3, 1);
        reward(Material.GOLD_ORE,          1, 1);
        reward(Material.COAL_ORE,          1, 0);
        reward(Material.LAPIS_ORE,         1, 0);
        reward(Material.NETHER_QUARTZ_ORE, 1, 0);
        reward(Material.REDSTONE_ORE,      1, 0);
    }

    static boolean dropSelf(Material material) {
        switch (material) {
        case IRON_ORE:
        case GOLD_ORE:
            return true;
        default: return false;
        }
    }

    boolean stone(@NonNull Block block) {
        switch (block.getType()) {
        case STONE:
        case DIORITE:
        case ANDESITE:
        case GRANITE:
            return true;
        default:
            return false;
        }
    }

    int stripMine(@NonNull Player player, @NonNull Block block,
                  @NonNull ItemStack item, final int fortune) {
        Block head = player.getEyeLocation().getBlock();
        int dx = block.getX() - head.getX();
        int dz = block.getZ() - head.getZ();
        if (dx == 0 && dz == 0) return 0;
        if (Math.abs(dx) > Math.abs(dz)) {
            dx /= Math.abs(dx);
            dz = 0;
        } else {
            dx = 0;
            dz /= Math.abs(dz);
        }
        Block nbor = block.getRelative(0, 0, 0);
        int result = 0;
        for (int i = 0; i < fortune; i += 1) {
            nbor = nbor.getRelative(dx, 0, dz);
            if (!stone(nbor)) break;
            if (!GenericEvents.playerCanBuild(player, nbor)) break;
            nbor.breakNaturally(item);
            result += 1;
        }
        return result;
    }

    int mineVein(@NonNull Player player, @NonNull Block block,
                @NonNull ItemStack item, Reward reward) {
        Material mat = block.getType();
        HashSet<Block> done = new HashSet<>();
        HashSet<Block> todo = new HashSet<>();
        HashSet<Block> vein = new HashSet<>();
        todo.add(block);
        done.add(block);
        while (!todo.isEmpty() && vein.size() < 20) {
            Block pivot = todo.iterator().next();
            todo.remove(pivot);
            for (BlockFace face : Arrays.asList(BlockFace.UP, BlockFace.NORTH, BlockFace.EAST,
                                                BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN)) {
                Block nbor = pivot.getRelative(face);
                if (done.contains(nbor)) continue;
                done.add(nbor);
                if (nbor.getType() != mat) continue;
                if (!GenericEvents.playerCanBuild(player, nbor)) continue;
                todo.add(nbor);
                vein.add(nbor);
            }
        }
        for (Block v : vein) {
            rewardMineBlock(player, block, reward);
            Effects.breakMagic(v);
            v.breakNaturally(item);
        }
        return vein.size();
    }

    boolean rewardMineBlock(@NonNull Player player, @NonNull Block block, @NonNull Reward reward) {
        if (Exploits.isPlayerPlaced(block)) return false;
        plugin.addSkillPoints(player, SkillType.MINING, reward.sp);
        if (reward.exp > 0) {
            block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5),
                                   ExperienceOrb.class,
                                   orb -> orb.setExperience(reward.exp));
        }
        if (block.getType() == Material.DIAMOND_ORE) {
            plugin.rollTalentPoint(player, 1);
        }
        return true;
    }

    void mine(@NonNull Player player, @NonNull Block block) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;
        int fortune = item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        Session session = plugin.sessionOf(player);
        if (stone(block) && fortune > 0
            && session.hasTalent(Talent.MINE_STRIP) && !player.isSneaking()) {
            stripMine(player, block, item, fortune);
        }
        Reward reward = rewards.get(block.getType());
        if (reward == null) return;
        if (fortune > 0 && session.hasTalent(Talent.MINE_STRIP) && !player.isSneaking()) {
            mineVein(player, block, item, reward);
        }
        rewardMineBlock(player, block, reward);
    }
}
