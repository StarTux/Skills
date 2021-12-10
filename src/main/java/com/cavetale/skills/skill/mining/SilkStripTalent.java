package com.cavetale.skills.skill.mining;

import com.cavetale.core.event.block.PlayerBlockAbilityQuery;
import com.cavetale.core.event.block.PlayerChangeBlockEvent;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.Util;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import com.destroystokyo.paper.MaterialTags;
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

public final class SilkStripTalent extends Talent implements Listener {
    protected final MiningSkill miningSkill;

    protected SilkStripTalent(final SkillsPlugin plugin, final MiningSkill miningSkill) {
        super(plugin, TalentType.MINE_SILK_STRIP);
        this.miningSkill = miningSkill;
    }

    @Override
    protected void enable() { }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem()) return;
        final ItemStack item = event.getItem();
        final Block block = event.getClickedBlock();
        if (!MaterialTags.PICKAXES.isTagged(item.getType())) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        MiningReward reward = miningSkill.rewards.get(block.getType());
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
            if (unbreaking == 0 || plugin.random.nextInt(unbreaking) == 0) {
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
        // (Maybe) change the Block
        double factor = 2.20; // Fortune 3
        Session session = plugin.sessions.of(player);
        if (session.isTalentEnabled(TalentType.MINE_SILK_MULTI)) factor = 2.60;
        final double amount; // Expected value of additionally dropped items.
        amount = (double) reward.drops * factor;
        final double chance; // Chance at NOT getting another drop.
        chance = 1.0 / amount;
        final double roll = plugin.random.nextDouble();
        Effects.useSilk(player, block, dropLocation);
        if (roll < chance) {
            miningSkill.giveReward(player, block, reward);
            if (reward.exp > 0) {
                Util.exp(dropLocation, reward.exp + session.getExpBonus(SkillType.MINING));
            }
            Effects.failSilk(player, block);
            new PlayerChangeBlockEvent(player, block, reward.replaceable.createBlockData()).callEvent();
            block.setType(reward.replaceable);
        }
        event.setCancelled(true);
    }
}
