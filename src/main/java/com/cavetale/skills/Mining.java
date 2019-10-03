package com.cavetale.skills;

import com.winthier.exploits.Exploits;
import java.util.EnumMap;
import lombok.NonNull;
import lombok.Value;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

    void mine(@NonNull Player player, @NonNull Block block) {
        Material material = block.getType();
        Reward reward = rewards.get(material);
        if (reward == null) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;
        // boolean silkTouch = item.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;
        // boolean dropSelf = dropSelf(material) || silkTouch;
        if (Exploits.isPlayerPlaced(block)) return;
        plugin.addSkillPoints(player, SkillType.MINING, reward.sp);
        if (reward.exp > 0) {
            block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5),
                                   ExperienceOrb.class,
                                   orb -> orb.setExperience(reward.exp));
        }
        if (block.getType() == Material.DIAMOND_ORE) {
            plugin.rollTalentPoint(player, 1);
        }
    }
}
