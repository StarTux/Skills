package com.cavetale.skills.skill.mining;

import com.cavetale.core.event.skills.SkillsBlockBreakRewardEvent;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.talent.TalentType;
import com.cavetale.skills.talent.mining.*;
import com.cavetale.skills.util.Players;
import com.destroystokyo.paper.MaterialTags;
import java.util.EnumMap;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.exploits.PlayerPlacedBlocks.isPlayerPlaced;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

@Getter
public final class MiningSkill extends Skill implements Listener {
    protected final EnumMap<Material, MiningReward> rewards = new EnumMap<>(Material.class);
    public final StripMiningTalent stripMiningTalent = new StripMiningTalent();
    public final VeinMiningTalent veinMiningTalent = new VeinMiningTalent();
    public final MineMagnetTalent mineMagnetTalent = new MineMagnetTalent();
    public final SilkStripTalent silkStripTalent = new SilkStripTalent();
    public final MinerSightTalent minerSightTalent = new MinerSightTalent();
    public final SuperVisionTalent superVisionTalent = new SuperVisionTalent();
    public final OreAlertTalent oreAlertTalent = new OreAlertTalent();

    public MiningSkill() {
        super(SkillType.MINING);
    }

    @Override
    protected void enable() {
        // exp values are maximal according to the wiki, used for Silk Stripping
        // veinExp values are averages rounded up, used for Vein Mining
        // Last three values are used by Silk Fortune, "null, 0, null" any values to disable
        reward(MiningReward.builder().material(Material.DIAMOND_ORE).sp(10).money(50.0).exp(7).veinExp(5).silkStripItem(Material.DIAMOND).drops(1).replaceable(Material.STONE).build());
        reward(MiningReward.builder().material(Material.DEEPSLATE_DIAMOND_ORE).sp(10).money(50.0).exp(7).veinExp(5).silkStripItem(Material.DIAMOND).drops(1).replaceable(Material.DEEPSLATE).build());
        reward(MiningReward.builder().material(Material.EMERALD_ORE).sp(10).money(30.0).exp(7).veinExp(5).silkStripItem(Material.EMERALD).drops(1).replaceable(Material.STONE).build());
        reward(MiningReward.builder().material(Material.DEEPSLATE_EMERALD_ORE).sp(10).money(30.0).exp(7).veinExp(5).silkStripItem(Material.EMERALD).drops(1).replaceable(Material.DEEPSLATE).build());
        reward(MiningReward.builder().material(Material.IRON_ORE).sp(3).money(10.0).exp(3).silkStripItem(Material.RAW_IRON).drops(1).replaceable(Material.STONE).build());
        reward(MiningReward.builder().material(Material.DEEPSLATE_IRON_ORE).sp(3).money(10.0).exp(3).silkStripItem(Material.RAW_IRON).drops(1).replaceable(Material.DEEPSLATE).build());
        reward(MiningReward.builder().material(Material.COPPER_ORE).sp(1).money(10.0).exp(3).silkStripItem(Material.RAW_COPPER).drops(4).replaceable(Material.STONE).build()); // 2-5
        reward(MiningReward.builder().material(Material.DEEPSLATE_COPPER_ORE).sp(1).money(10.0).exp(3).silkStripItem(Material.RAW_COPPER).drops(4).replaceable(Material.DEEPSLATE).build());
        reward(MiningReward.builder().material(Material.GOLD_ORE).sp(5).money(10.0).exp(3).silkStripItem(Material.RAW_GOLD).drops(1).replaceable(Material.STONE).build());
        reward(MiningReward.builder().material(Material.DEEPSLATE_GOLD_ORE).sp(5).money(10.0).exp(3).silkStripItem(Material.RAW_GOLD).drops(1).replaceable(Material.DEEPSLATE).build());
        reward(MiningReward.builder().material(Material.NETHER_GOLD_ORE).sp(5).money(10.0).exp(1).veinExp(1).silkStripItem(Material.GOLD_NUGGET).drops(4).replaceable(Material.NETHERRACK).build()); // 2-6
        reward(MiningReward.builder().material(Material.GILDED_BLACKSTONE).sp(5).money(10.0).silkStripItem(Material.GOLD_NUGGET).replaceable(Material.BLACKSTONE).build()); // 10% to drop 2-5
        reward(MiningReward.builder().material(Material.COAL_ORE).sp(1).money(1.0).exp(2).veinExp(1).silkStripItem(Material.COAL).drops(1).replaceable(Material.STONE).build());
        reward(MiningReward.builder().material(Material.DEEPSLATE_COAL_ORE).sp(1).money(1.0).exp(2).veinExp(1).silkStripItem(Material.COAL).drops(1).replaceable(Material.DEEPSLATE).build());
        reward(MiningReward.builder().material(Material.LAPIS_ORE).sp(8).money(40.0).exp(5).veinExp(4).silkStripItem(Material.LAPIS_LAZULI).drops(7).replaceable(Material.STONE).build()); // 4-9
        reward(MiningReward.builder().material(Material.DEEPSLATE_LAPIS_ORE).sp(8).money(40.0).exp(5).veinExp(4).silkStripItem(Material.LAPIS_LAZULI).drops(7).replaceable(Material.DEEPSLATE).build());
        reward(MiningReward.builder().material(Material.NETHER_QUARTZ_ORE).sp(1).money(1.0).exp(5).veinExp(4).silkStripItem(Material.QUARTZ).drops(1).replaceable(Material.NETHERRACK).build());
        reward(MiningReward.builder().material(Material.REDSTONE_ORE).sp(1).money(3.0).exp(5).veinExp(3).silkStripItem(Material.REDSTONE).drops(5).replaceable(Material.STONE).build()); // 4-5
        reward(MiningReward.builder().material(Material.DEEPSLATE_REDSTONE_ORE).sp(1).money(3.0).exp(5).veinExp(3).silkStripItem(Material.REDSTONE).drops(5).replaceable(Material.DEEPSLATE).build());
        // technically not ores
        reward(MiningReward.builder().material(Material.ANCIENT_DEBRIS).sp(10).money(50.0).exp(10).silkStripItem(Material.NETHERITE_SCRAP).drops(1).replaceable(Material.NETHERRACK).build());
        reward(MiningReward.builder().material(Material.RAW_COPPER_BLOCK).sp(5).money(50.0).exp(3).build());
        reward(MiningReward.builder().material(Material.RAW_IRON_BLOCK).sp(15).money(50.0).exp(3).build());
        reward(MiningReward.builder().material(Material.RAW_GOLD_BLOCK).sp(25).money(50.0).exp(3).build()); // currently does not generate
        reward(MiningReward.builder().material(Material.BUDDING_AMETHYST).sp(10).money(10.0).exp(1).silkStripItem(Material.AMETHYST_SHARD).drops(2).replaceable(Material.AMETHYST_BLOCK).build());
        reward(MiningReward.builder().material(Material.GLOWSTONE).sp(1).money(1.0).exp(1).silkStripItem(Material.GLOWSTONE).build());
    }

    private void reward(MiningReward miningReward) {
        rewards.put(miningReward.getMaterial(), miningReward);
    }

    protected static boolean netherStone(@NonNull Block block) {
        switch (block.getType()) {
        case NETHERRACK:
        case BLACKSTONE:
        case BASALT:
            return true;
        default:
            return false;
        }
    }

    protected static boolean netherDirt(@NonNull Block block) {
        switch (block.getType()) {
        case GRAVEL:
        case SOUL_SOIL:
            return true;
        default:
            return false;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!Players.playMode(player)) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !MaterialTags.PICKAXES.isTagged(item.getType())) return;
        Block block = event.getBlock();
        MiningReward reward = rewards.get(block.getType());
        if (reward != null && veinMiningTalent.tryToVeinMine(player, item, block, reward, event)) {
            return;
        }
        stripMiningTalent.onWillBreakBlock(player, block);
        minerSightTalent.onWillBreakBlock(player, block);
        if (reward == null) return;
        final Location dropLocation = Session.of(player).isTalentEnabled(TalentType.MINE_MAGNET)
            ? player.getLocation()
            : block.getLocation().add(0.5, 0.25, 0.0);
        giveReward(player, block, reward, dropLocation);
    }

    public MiningReward getReward(Material material) {
        return rewards.get(material);
    }

    public MiningReward getReward(Block block) {
        return rewards.get(block.getType());
    }

    /**
     * Give the SP reward for the broken block and roll talent points
     * where it applies.
     */
    public boolean giveReward(Player player, Block block, MiningReward reward, Location dropLocation) {
        if (isPlayerPlaced(block)) return false;
        final Session session = Session.of(player);
        if (!session.isEnabled()) return false;
        final var rewardEvent = new SkillsBlockBreakRewardEvent(player, block,
                                                                reward.sp,
                                                                session.computeMoneyDrop(skillType, reward.money),
                                                                reward.exp + session.getExpBonus(skillType));
        rewardEvent.callEvent();
        if (rewardEvent.isCancelled()) return false;
        if (rewardEvent.getPostMultiplyFactor() != 1.0) {
            skillsPlugin().getLogger().info("[Mining] [" + reward.getMaterial() + "] " + rewardEvent.debugString());
        }
        session.addSkillPoints(skillType, rewardEvent.getFinalSkillPoints());
        dropMoney(player, dropLocation, rewardEvent.getFinalMoney());
        if (rewardEvent.getFinalExp() > 0) {
            player.giveExp(rewardEvent.getFinalExp(), true);
        }
        return true;
    }

    public boolean giveStackedReward(Player player, ItemStack item, List<Block> blocks, MiningReward reward, Location dropLocation, int stackCount) {
        final Session session = Session.of(player);
        if (!session.isEnabled()) return false;
        final var rewardEvent = new SkillsBlockBreakRewardEvent(player, blocks,
                                                                reward.sp * stackCount,
                                                                session.computeMoneyDrop(skillType, reward.money * (double) stackCount),
                                                                (reward.exp + reward.veinExp + session.getExpBonus(skillType)) * stackCount);
        rewardEvent.callEvent();
        if (rewardEvent.isCancelled()) return false;
        if (rewardEvent.getPostMultiplyFactor() != 1.0) {
            skillsPlugin().getLogger().info("[Mining] [" + stackCount + "x" + reward.getMaterial() + "] " + player.getName() + " " + rewardEvent.debugString());
        }
        session.addSkillPoints(skillType, rewardEvent.getFinalSkillPoints());
        dropMoney(player, dropLocation, rewardEvent.getFinalMoney());
        if (rewardEvent.getFinalExp() > 0) {
            player.giveExp(rewardEvent.getFinalExp(), true);
        }
        return true;
    }
}
