package com.cavetale.skills.skill.mining;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.MaterialTags;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class MinerSightTalent extends Talent implements Listener {
    protected final MiningSkill miningSkill;

    protected MinerSightTalent(final SkillsPlugin plugin, final MiningSkill miningSkill) {
        super(plugin, TalentType.MINER_SIGHT);
        this.miningSkill = miningSkill;
    }

    @Override
    protected void enable() { }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        Block block = event.getBlock();
        if (!MiningSkill.anyStone(block)) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        if (!MaterialTags.PICKAXES.isTagged(item.getType())) return;
        if (player.isSneaking()) return;
        PotionEffect nightVision = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
        if (nightVision == null || nightVision.getDuration() < 20 * 20) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                                                    60 * 20, // ticks
                                                    0, // amplifier
                                                    true, // ambient
                                                    false, // particles
                                                    true)); // icon
        }
    }
}
