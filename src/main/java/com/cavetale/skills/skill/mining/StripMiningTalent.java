package com.cavetale.skills.skill.mining;

import com.cavetale.core.event.block.PlayerBlockAbilityQuery;
import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import com.destroystokyo.paper.MaterialTags;
import com.winthier.exploits.Exploits;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public final class StripMiningTalent extends Talent implements Listener {
    protected final MiningSkill miningSkill;

    protected StripMiningTalent(final SkillsPlugin plugin, final MiningSkill miningSkill) {
        super(plugin, TalentType.MINE_STRIP);
        this.miningSkill = miningSkill;
    }

    @Override
    protected void enable() { }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        final Block block = event.getBlock();
        final boolean sneak = player.isSneaking();
        final boolean stone = MiningSkill.stone(block);
        if (!sneak && stone) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!player.isValid()) return;
                    if (!player.getWorld().equals(block.getWorld())) return;
                    stripMine(player, block);
                });
        }
        // Vein Mining
        final ItemStack item = player.getInventory().getItemInMainHand();
        final int efficiency = item.getEnchantmentLevel(Enchantment.DIG_SPEED);
        MiningReward reward = miningSkill.rewards.get(block.getType());
        if (!sneak && efficiency > 0 && reward != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!player.isValid()) return;
                    if (!player.getWorld().equals(block.getWorld())) return;
                    mineVein(player, block, item, reward, efficiency);
                });
        }
    }

    /**
     * Called via scheduler.
     */
    protected int stripMine(@NonNull Player player, @NonNull Block block) {
        // Check item
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return 0;
        if (!MaterialTags.PICKAXES.isTagged(item.getType())) return 0;
        int efficiency = item.getEnchantmentLevel(Enchantment.DIG_SPEED);
        if (efficiency <= 0) return 0;
        // Figure out direction
        Block head = player.getEyeLocation().getBlock();
        // Require straight mining
        if (head.getX() != block.getX()
            && head.getZ() != block.getZ()) return 0;
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
        // Figure out item
        Damageable dmg = null;
        ItemMeta meta = item.getItemMeta();
        int unbreaking = item.getEnchantmentLevel(Enchantment.DURABILITY);
        if (!meta.isUnbreakable() && meta instanceof Damageable) {
            dmg = (Damageable) meta;
        }
        // Start breaking
        Block nbor = block.getRelative(0, 0, 0); // clone
        int result = 0;
        int total = efficiency / 2 + 1;
        for (int i = 0; i < total; i += 1) {
            nbor = nbor.getRelative(dx, 0, dz);
            if (!MiningSkill.stone(nbor)) break;
            if (!PlayerBlockAbilityQuery.Action.BUILD.query(player, nbor)) return result;
            // Damage the pickaxe and cancel if it is used up.
            if (dmg != null) {
                if (dmg.getDamage() >= item.getType().getMaxDurability()) break;
                if (unbreaking == 0 || plugin.random.nextInt(unbreaking) == 0) {
                    dmg.setDamage(dmg.getDamage() + 1);
                    item.setItemMeta(meta);
                }
            }
            if (!PlayerBreakBlockEvent.call(player, nbor)) return result;
            Effects.mineBlockMagic(nbor);
            nbor.breakNaturally(item);
            result += 1;
        }
        return result;
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
        for (Block v : vein) {
            if (!Exploits.isPlayerPlaced(v)) {
                miningSkill.giveReward(player, v, reward);
            }
            Bukkit.getPluginManager().callEvent(new PlayerBreakBlockEvent(player, v));
            Effects.mineBlockMagic(v);
            v.breakNaturally(item);
        }
        return vein.size();
    }
}
