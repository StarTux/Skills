package com.cavetale.skills.talent.mining;

import com.cavetale.core.event.block.PlayerBlockAbilityQuery;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import static com.cavetale.skills.SkillsPlugin.miningSkill;
import static com.cavetale.skills.SkillsPlugin.random;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class StripMiningTalent extends Talent {
    public StripMiningTalent() {
        super(TalentType.STRIP_MINING, "Strip Mining",
              "Mining stone with a :diamond_pickaxe:pickaxe breaks several blocks ahead of you.",
              "Unleash the full power of the Efficency enchantment. Mining stone type blocks will break several blocks within a line while mining straight. Stone includes:"
              + "\n:stone:Stone"
              + "\n:andesite:Andesite"
              + "\n:diorite:Diorite"
              + "\n:granite:Granite"
              + "\n:deepslate:Deepslate (dense)"
              + "\n:tuff:Tuff (dense)"
              + "\nMine without this feature by sneaking.");
        addLevel(1, levelToRange(1) + " blocks (half for dense stones)");
        addLevel(1, levelToRange(2) + " blocks (half for dense stones)");
        addLevel(1, levelToRange(3) + " blocks (half for dense stones)");
        addLevel(1, levelToRange(4) + " blocks (half for dense stones)");
        addLevel(1, levelToRange(5) + " blocks (half for dense stones)");
    }

    private static int levelToRange(int level) {
        return 1 + level;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.STONE);
    }

    private int getStoneHardness(Block block) {
        return switch (block.getType()) {
        case STONE -> 1;
        case DIORITE -> 1;
        case ANDESITE -> 1;
        case GRANITE -> 1;
        case DEEPSLATE -> 2;
        case TUFF -> 2;
        default -> 0;
        };
    }

    public boolean onWillBreakBlock(Player player, Block block) {
        if (!isPlayerEnabled(player)) return false;
        if (player.isSneaking()) return false;
        if (0 == getStoneHardness(block)) return false;
        final ItemStack pickaxe = player.getInventory().getItemInMainHand();
        if (pickaxe == null || !MaterialTags.PICKAXES.isTagged(pickaxe.getType())) {
            return false;
        }
        final int level = getTalentLevel(player);
        if (level < 1) return false;
        final int range = levelToRange(level);
        Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                if (!player.isValid()) return;
                if (!player.getWorld().equals(block.getWorld())) return;
                stripMine(player, block, range);
            });
        return true;
    }

    /**
     * Called via scheduler.
     */
    private int stripMine(Player player, final Block block, final int range) {
        // Figure out direction
        final Block head = player.getEyeLocation().getBlock();
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
        final ItemStack pickaxe = player.getInventory().getItemInMainHand();
        if (pickaxe == null || !MaterialTags.PICKAXES.isTagged(pickaxe.getType())) return 0;
        if (!(pickaxe.getItemMeta() instanceof Damageable damageable)) return 0;
        final int unbreaking = damageable.getEnchantLevel(Enchantment.UNBREAKING);
        final int durability = pickaxe.getType().getMaxDurability();
        // Start breaking
        Block nbor = block.getRelative(0, 0, 0); // clone
        int blocksMined = 0;
        int charges = range;
        for (int i = 0; i < range; i += 1) {
            nbor = nbor.getRelative(dx, 0, dz);
            final int stoneHardness = getStoneHardness(nbor);
            if (stoneHardness == 0) break;
            if (charges < stoneHardness) break;
            if (!PlayerBlockAbilityQuery.Action.BUILD.query(player, nbor)) break;
            // Damage the pickaxe and cancel if it is used up.
            if (!damageable.isUnbreakable()) {
                if (damageable.getDamage() >= durability) break;
                if (unbreaking == 0 || random().nextInt(unbreaking) == 0) {
                    damageable.setDamage(damageable.getDamage() + 1);
                }
            }
            miningSkill().getMinerSightTalent().onWillBreakBlock(player, nbor);
            if (!miningSkill().getMineMagnetTalent().breakBlock(player, pickaxe, nbor)) break;
            charges -= stoneHardness;
            blocksMined += 1;
        }
        pickaxe.setItemMeta(damageable);
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + " lvl:" + getTalentLevel(player) + " range:" + range + " mined:" + blocksMined);
        }
        return blocksMined;
    }
}
