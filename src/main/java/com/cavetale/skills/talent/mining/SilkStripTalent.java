package com.cavetale.skills.talent.mining;

import com.cavetale.core.event.block.PlayerBlockAbilityQuery;
import com.cavetale.core.event.block.PlayerChangeBlockEvent;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.mining.MiningReward;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import static com.cavetale.core.exploits.PlayerPlacedBlocks.isPlayerPlaced;
import static com.cavetale.skills.SkillsPlugin.miningSkill;
import static com.cavetale.skills.SkillsPlugin.random;

public final class SilkStripTalent extends Talent implements Listener {
    public SilkStripTalent() {
        super(TalentType.SILK_STRIP, "Silk Stripping",
              "Use a :diamond_pickaxe:Silk Touch pickaxe to strip a natural :diamond_ore:ore of its contents.",
              ":mouse_right: with a Silk Touch pickaxe to use your fine motory skills and remove those treasures right from the ore block.With any luck, you may repeat the procedure as long as the ore stays intact, getting more and more drops.",
              "Eventually, the ore will turn into stone and you get the usual skill points for mining. This method may yield as much reward as Fortune IV would but with greater variance.",
              "Silk Stripping only works on natural ores. Picking up and moving the ore will compromise its structural integrity, making Silk Stripping ineffective.");
        addLevel(1, levelToPercentage(1) + "% drop chance (Fortune IV)");
        addLevel(1, levelToPercentage(2) + "% drop chance");
        addLevel(1, levelToPercentage(3) + "% drop chance (Fortune V)");
        addLevel(1, levelToPercentage(4) + "% drop chance");
        addLevel(1, levelToPercentage(5) + "% drop chance (Fortune VI)");
    }

    // Calculation: https://minecraft.fandom.com/wiki/Fortune#Ore
    // 1/(lvl+2) + (lvl + 1)/2
    // Fortune 3 => 2.20
    // Fortune 4 => 2.6666
    // Fortune 5 => 3.1428
    // Fortune 6 => 3.625
    private double levelToPercentage(int level) {
        return switch (level) {
        case 1 -> 266;
        case 2 -> 290;
        case 3 -> 314;
        case 4 -> 338;
        case 5 -> 362;
        default -> 0;
        };
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.GOLD_NUGGET);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem()) return;
        final ItemStack item = event.getItem();
        final Block block = event.getClickedBlock();
        if (isPlayerPlaced(block)) return;
        if (!MaterialTags.PICKAXES.isTagged(item.getType())) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        final MiningReward reward = miningSkill().getReward(block.getType());
        if (reward == null || reward.getSilkStripItem() == null || reward.getDrops() <= 0 || reward.getReplaceable() == null) {
            return;
        }
        if (item == null || item.getType() == Material.AIR) return;
        if (!PlayerBlockAbilityQuery.Action.BUILD.query(player, block)) return;
        final int silk = item.getEnchantmentLevel(Enchantment.SILK_TOUCH);
        if (silk == 0) return;
        // Damage the pickaxe
        ItemMeta meta = item.getItemMeta();
        if (!meta.isUnbreakable() && meta instanceof Damageable) {
            Damageable dmg = (Damageable) meta;
            if (dmg.getDamage() >= item.getType().getMaxDurability()) return;
            int unbreaking = item.getEnchantmentLevel(Enchantment.UNBREAKING);
            if (unbreaking == 0 || random().nextInt(unbreaking) == 0) {
                dmg.setDamage(dmg.getDamage() + 1);
                item.setItemMeta(meta);
            }
        }
        // Drop an item (point of no return)
        final ItemStack drop = new ItemStack(reward.getSilkStripItem());
        final BlockFace face = event.getBlockFace();
        final double off = 0.7;
        final Location dropLocation = block.getLocation().add(0.5 + (double) face.getModX() * off,
                                                              0.5 + (double) face.getModY() * off,
                                                              0.5 + (double) face.getModZ() * off);
        if (face.getModY() == -1) {
            dropLocation.add(0, -0.5, 0);
        } else if (face.getModY() != 1) {
            dropLocation.add(0, -0.25, 0);
        }
        final double spd = 0.125;
        final Vector vel = new Vector(face.getModX() * spd,
                                      face.getModY() * spd,
                                      face.getModZ() * spd);
        player.getWorld().dropItem(dropLocation, drop).setVelocity(vel);
        final Session session = Session.of(player);
        final int percentage = session.getTalentLevel(talentType);
        final double factor = percentage * 0.01;
        // Expected value of additionally dropped items.
        final double amount = (double) reward.getDrops() * factor;
        // Chance at NOT getting another drop.
        final double chance = amount > 0.01
            ? 1.0 / amount
            : 1.0;
        final double roll = random().nextDouble();
        block.getWorld().playSound(block.getLocation().add(0.5, 0.5, 0.5), Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.BLOCKS, 1.0f, 2.0f);
        block.getWorld().spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5), 3, 0.0, 0.0, 0.0, 0.0, block.getBlockData());
        if (roll < chance) {
            miningSkill().giveReward(player, block, reward, dropLocation);
            block.getWorld().playSound(block.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.0f, 2.0f);
            new PlayerChangeBlockEvent(player, block, reward.getReplaceable().createBlockData()).callEvent();
            block.setType(reward.getReplaceable());
        }
        event.setCancelled(true);
    }
}
