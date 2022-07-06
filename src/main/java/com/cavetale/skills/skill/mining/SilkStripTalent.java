package com.cavetale.skills.skill.mining;

import com.cavetale.core.event.block.PlayerBlockAbilityQuery;
import com.cavetale.core.event.block.PlayerChangeBlockEvent;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import com.destroystokyo.paper.MaterialTags;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
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
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class SilkStripTalent extends Talent implements Listener {
    protected SilkStripTalent() {
        super(TalentType.SILK_STRIP);
    }

    @Override
    public String getDisplayName() {
        return "Silk Stripping";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Use a Silk Touch pickaxe to strip a natural ore of its contents",
                       "Right-click with a Silk Touch pickaxe to use your"
                       + " fine motory skills and remove those"
                       + " treasures right from the ore block."
                       + "With any luck, you may repeat the procedure"
                       + " as long as the ore stays intact,"
                       + " getting more and more drops.",
                       "Eventually, the ore will turn into stone and"
                       + " you get the usual skill points for mining."
                       + " This method may yield as much reward as Fortune IV"
                       + " would but with greater variance.",
                       "Silk Stripping only works on natural ores."
                       + " Picking up and moving the ore will compromise its structural integrity,"
                       + " making Silk Stripping ineffective.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.GOLD_NUGGET);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem()) return;
        final ItemStack item = event.getItem();
        final Block block = event.getClickedBlock();
        if (isPlayerPlaced(block)) return;
        final boolean metal = MiningSkill.metalOre(block);
        if (!MaterialTags.PICKAXES.isTagged(item.getType())) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        MiningReward reward = miningSkill().rewards.get(block.getType());
        if (reward == null || reward.item == null || reward.drops <= 0) return;
        if (item == null || item.getType() == Material.AIR) return;
        if (!PlayerBlockAbilityQuery.Action.BUILD.query(player, block)) return;
        int silk = item.getEnchantmentLevel(Enchantment.SILK_TOUCH);
        if (silk == 0) return;
        // Damage the pickaxe
        ItemMeta meta = item.getItemMeta();
        if (!meta.isUnbreakable() && meta instanceof Damageable) {
            Damageable dmg = (Damageable) meta;
            if (dmg.getDamage() >= item.getType().getMaxDurability()) return;
            int unbreaking = item.getEnchantmentLevel(Enchantment.DURABILITY);
            if (unbreaking == 0 || random().nextInt(unbreaking) == 0) {
                dmg.setDamage(dmg.getDamage() + 1);
                item.setItemMeta(meta);
            }
        }
        // Drop an item (point of no return)
        ItemStack drop = new ItemStack(reward.item);
        BlockFace face = event.getBlockFace();
        double off = 0.7;
        Location dropLocation = block
            .getLocation().add(0.5 + (double) face.getModX() * off,
                               0.5 + (double) face.getModY() * off,
                               0.5 + (double) face.getModZ() * off);
        if (face.getModY() == -1) {
            dropLocation = dropLocation.add(0, -0.5, 0);
        } else if (face.getModY() != 1) {
            dropLocation = dropLocation.add(0, -0.25, 0);
        }
        double spd = 0.125;
        Vector vel = new Vector(face.getModX() * spd,
                                face.getModY() * spd,
                                face.getModZ() * spd);
        player.getWorld().dropItem(dropLocation, drop).setVelocity(vel);
        // Calculation: https://minecraft.fandom.com/wiki/Fortune#Ore
        // 1/(lvl+2) + (lvl + 1)/2
        // Fortune 3 => 2.20
        // Fortune 4 => 2.6666
        // Fortune 5 => 3.1428
        // Fortune 6 => 3.625
        double factor = 2.6666;
        Session session = sessionOf(player);
        if (metal && session.isTalentEnabled(TalentType.SILK_METALS)) factor = 3.1428;
        if (!metal && session.isTalentEnabled(TalentType.SILK_MULTI)) factor = 3.1428;
        final double amount; // Expected value of additionally dropped items.
        amount = (double) reward.drops * factor;
        final double chance; // Chance at NOT getting another drop.
        chance = 1.0 / amount;
        final double roll = random().nextDouble();
        Effects.useSilk(player, block, dropLocation);
        if (roll < chance) {
            miningSkill().giveReward(player, block, reward, dropLocation);
            Effects.failSilk(player, block);
            new PlayerChangeBlockEvent(player, block, reward.replaceable.createBlockData()).callEvent();
            block.setType(reward.replaceable);
        }
        event.setCancelled(true);
        if (session.isDebugMode()) {
            player.sendMessage(talentType + " metal=" + metal + " factor=" + factor);
        }
    }
}
