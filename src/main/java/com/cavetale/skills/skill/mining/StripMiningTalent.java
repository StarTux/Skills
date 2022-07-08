package com.cavetale.skills.skill.mining;

import com.cavetale.core.event.block.PlayerBlockAbilityQuery;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import static com.cavetale.skills.SkillsPlugin.miningSkill;
import static com.cavetale.skills.SkillsPlugin.random;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class StripMiningTalent extends Talent implements Listener {
    protected StripMiningTalent() {
        super(TalentType.STRIP_MINING);
    }

    @Override
    public String getDisplayName() {
        return "Strip Mining";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Mining stone with an Efficiency pickaxe breaks many blocks",
                       "Unleash the full power of the Efficency enchantment."
                       + " Mining stone type blocks will break several blocks"
                       + " within a line while mining straight."
                       + " Stone includes: Stone, Andesite, Diorite, Granite",
                       "Mine without this feature by sneaking.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.STONE);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        final Block block = event.getBlock();
        if (player.isSneaking()) return;
        boolean hasDeep = sessionOf(player).isTalentEnabled(TalentType.DEEP_MINING);
        final boolean stone = MiningSkill.stone(block) || (hasDeep && MiningSkill.deepStone(block));
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " hasDeep=" + hasDeep + " stone=" + stone);
        }
        if (!stone) return;
        Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                if (!player.isValid()) return;
                if (!player.getWorld().equals(block.getWorld())) return;
                stripMine(player, block, hasDeep);
            });
    }

    /**
     * Called via scheduler.
     */
    protected int stripMine(@NonNull Player player, @NonNull Block block, boolean hasDeep) {
        // Check item
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return 0;
        if (!MaterialTags.PICKAXES.isTagged(item.getType())) return 0;
        int efficiency = item.getEnchantmentLevel(Enchantment.DIG_SPEED);
        if (efficiency <= 0) return 0;
        // Figure out direction
        Block head = player.getEyeLocation().getBlock();
        // Require straight mining
        if (head.getX() != block.getX() && head.getZ() != block.getZ()) return 0;
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
            if (hasDeep && !MiningSkill.anyStone(nbor)) {
                break;
            } else if (!hasDeep && !MiningSkill.stone(nbor)) {
                break;
            }
            if (!PlayerBlockAbilityQuery.Action.BUILD.query(player, nbor)) return result;
            // Damage the pickaxe and cancel if it is used up.
            if (dmg != null) {
                if (dmg.getDamage() >= item.getType().getMaxDurability()) break;
                if (unbreaking == 0 || random().nextInt(unbreaking) == 0) {
                    dmg.setDamage(dmg.getDamage() + 1);
                    item.setItemMeta(meta);
                }
            }
            if (!miningSkill().mineMagnetTalent.breakBlock(player, item, nbor)) return result;
            result += 1;
        }
        return result;
    }
}
