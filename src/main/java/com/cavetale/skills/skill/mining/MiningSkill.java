package com.cavetale.skills.skill.mining;

import com.cavetale.core.event.skills.SkillsBlockBreakRewardEvent;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Players;
import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import java.util.EnumMap;
import java.util.List;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.exploits.PlayerPlacedBlocks.isPlayerPlaced;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static org.bukkit.Material.*;

public final class MiningSkill extends Skill implements Listener {
    protected final EnumMap<Material, MiningReward> rewards = new EnumMap<>(Material.class);
    public final StripMiningTalent stripMiningTalent = new StripMiningTalent();
    public final DeepMiningTalent deepMiningTalent = new DeepMiningTalent();
    public final VeinMiningTalent veinMiningTalent = new VeinMiningTalent();
    public final MineMagnetTalent mineMagnetTalent = new MineMagnetTalent();
    public final VeinGemsTalent veinGemsTalent = new VeinGemsTalent();
    public final VeinMetalsTalent veinMetalsTalent = new VeinMetalsTalent();
    public final SilkStripTalent silkStripTalent = new SilkStripTalent();
    public final SilkMetalsTalent silkMetalsTalent = new SilkMetalsTalent();
    public final SilkMultiTalent silkMultiTalent = new SilkMultiTalent();
    public final MinerSightTalent minerSightTalent = new MinerSightTalent();
    public final SuperVisionTalent superVisionTalent = new SuperVisionTalent();
    public final DeepVisionTalent deepVisionTalent = new DeepVisionTalent();
    public final NetherVisionTalent netherVisionTalent = new NetherVisionTalent();
    public final OreAlertTalent oreAlertTalent = new OreAlertTalent();
    public final EmeraldAlertTalent emeraldAlertTalent = new EmeraldAlertTalent();
    public final DebrisAlertTalent debrisAlertTalent = new DebrisAlertTalent();
    public final RubyTalent rubyTalent = new RubyTalent();
    private static final MaterialSetTag STONE_TYPES = new
        MaterialSetTag(NamespacedKey.fromString("skills:stone_types"),
                       STONE, DIORITE, ANDESITE, GRANITE).lock();
    private static final MaterialSetTag DEEP_STONE_TYPES = new
        MaterialSetTag(NamespacedKey.fromString("skills:deep_stone_types"),
                       DEEPSLATE, TUFF).lock();
    private static final MaterialSetTag ALL_STONE_TYPES = new
        MaterialSetTag(NamespacedKey.fromString("skills:all_stone_types"))
        .add(STONE_TYPES.getValues())
        .add(DEEP_STONE_TYPES.getValues()).lock();

    public MiningSkill() {
        super(SkillType.MINING);
    }

    @Override
    protected void enable() {
        // exp values are maximal according to the wiki, used for Silk Stripping
        // veinExp values are averages rounded up, used for Vein Mining
        // Last three values are used by Silk Fortune, "null, 0, null" any values to disable
        reward(DIAMOND_ORE, 10, 50.0, 7, 5, DIAMOND, 1, STONE);
        reward(DEEPSLATE_DIAMOND_ORE, 10, 50.0, 7, 5, DIAMOND, 1, DEEPSLATE);
        reward(EMERALD_ORE, 10, 30.0, 7, 5, EMERALD, 1, STONE);
        reward(DEEPSLATE_EMERALD_ORE, 10, 30.0, 7, 5, EMERALD, 1, DEEPSLATE);
        reward(IRON_ORE, 3, 10.0, 3, 0, RAW_IRON, 1, STONE);
        reward(DEEPSLATE_IRON_ORE, 3, 10.0, 3, 0, RAW_IRON, 1, DEEPSLATE);
        reward(COPPER_ORE, 1, 10.0, 3, 0, RAW_COPPER, 4, STONE); // 2-5
        reward(DEEPSLATE_COPPER_ORE, 1, 10.0, 3, 0, RAW_COPPER, 4, DEEPSLATE);
        reward(GOLD_ORE, 5, 10.0, 3, 0, RAW_GOLD, 1, STONE);
        reward(DEEPSLATE_GOLD_ORE, 5, 10.0, 3, 0, RAW_GOLD, 1, DEEPSLATE);
        reward(NETHER_GOLD_ORE, 5, 10.0, 1, 1, GOLD_NUGGET, 4, NETHERRACK); // 2-6
        reward(GILDED_BLACKSTONE, 5, 10.0, 0, 0, GOLD_NUGGET, 0, BLACKSTONE); // 10% to drop 2-5
        reward(COAL_ORE, 1, 1.0, 2, 1, COAL, 1, STONE);
        reward(DEEPSLATE_COAL_ORE, 1, 1.0, 2, 1, COAL, 1, DEEPSLATE);
        reward(LAPIS_ORE, 5, 1.0, 5, 4, LAPIS_LAZULI, 7, STONE); // 4-9
        reward(DEEPSLATE_LAPIS_ORE, 5, 1.0, 5, 4, LAPIS_LAZULI, 7, DEEPSLATE);
        reward(NETHER_QUARTZ_ORE, 1, 1.0, 5, 4, QUARTZ, 1, NETHERRACK);
        reward(REDSTONE_ORE, 1, 1.0, 5, 3, REDSTONE, 5, STONE); // 4-5
        reward(DEEPSLATE_REDSTONE_ORE, 1, 1.0, 5, 3, REDSTONE, 5, DEEPSLATE);
        // technically not ores
        reward(ANCIENT_DEBRIS, 10, 50.0, 10, 0, NETHERITE_SCRAP, 1, NETHERRACK);
        reward(RAW_COPPER_BLOCK, 5, 50.0, 3, 0, null, 0, null);
        reward(RAW_IRON_BLOCK, 15, 50.0, 3, 0, null, 0, null);
        reward(RAW_GOLD_BLOCK, 25, 50.0, 3, 0, null, 0, null); // currently does not generate
        reward(BUDDING_AMETHYST, 10, 10.0, 1, 0, AMETHYST_SHARD, 2, AMETHYST_BLOCK);
    }

    private void reward(Material material, int sp, double money, int exp, int veinExp, Material item, int drops, Material replaceable) {
        rewards.put(material, new MiningReward(material, sp, money, exp, veinExp, item, drops, replaceable));
    }

    protected static boolean stone(@NonNull Block block) {
        return STONE_TYPES.isTagged(block.getType());
    }

    protected static boolean deepStone(@NonNull Block block) {
        return DEEP_STONE_TYPES.isTagged(block.getType());
    }

    protected static boolean anyStone(Block block) {
        return ALL_STONE_TYPES.isTagged(block.getType());
    }

    private static final MaterialSetTag DIRT_TYPES = new MaterialSetTag(NamespacedKey.fromString("skills:dirt_types"),
                                                                        GRAVEL, DIRT).lock();

    protected static boolean dirt(@NonNull Block block) {
        return DIRT_TYPES.isTagged(block.getType());
    }

    protected static boolean metalOre(@NonNull Block block) {
        switch (block.getType()) {
        case COPPER_ORE:
        case DEEPSLATE_COPPER_ORE:
        case IRON_ORE:
        case DEEPSLATE_IRON_ORE:
        case GOLD_ORE:
        case DEEPSLATE_GOLD_ORE:
        case NETHER_GOLD_ORE:
        case GILDED_BLACKSTONE:
        case ANCIENT_DEBRIS:
            return true;
        default:
            return false;
        }
    }

    protected static boolean gemOre(@NonNull Block block) {
        switch (block.getType()) {
        case DIAMOND_ORE:
        case DEEPSLATE_DIAMOND_ORE:
        case EMERALD_ORE:
        case DEEPSLATE_EMERALD_ORE:
        case NETHER_QUARTZ_ORE:
            return true;
        default:
            return false;
        }
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
        case SOUL_SAND:
        case SOUL_SOIL:
            return true;
        default:
            return false;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!Players.playMode(player)) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !MaterialTags.PICKAXES.isTagged(item.getType())) return;
        Block block = event.getBlock();
        MiningReward reward = rewards.get(block.getType());
        if (reward == null) return;
        if (veinMiningTalent.tryToVeinMine(player, item, block, reward, event)) {
            return;
        }
        final Location dropLocation = sessionOf(player).isTalentEnabled(TalentType.MINE_MAGNET)
            ? player.getLocation()
            : block.getLocation().add(0.5, 0.25, 0.0);
        giveReward(player, block, reward, dropLocation);
    }

    /**
     * Give the SP reward for the broken block and roll talent points
     * where it applies.
     */
    protected boolean giveReward(Player player, Block block, MiningReward reward, Location dropLocation) {
        if (isPlayerPlaced(block)) return false;
        Session session = sessionOf(player);
        if (!session.isEnabled()) return false;
        final var rewardEvent = new SkillsBlockBreakRewardEvent(player, block,
                                                                reward.sp,
                                                                session.computeMoneyDrop(skillType, reward.money),
                                                                reward.exp + session.getExpBonus(skillType));
        rewardEvent.callEvent();
        if (rewardEvent.isCancelled()) return false;
        skillsPlugin().getLogger().info("[Mining] [" + block.getType() + "] " + rewardEvent.debugString());
        session.addSkillPoints(skillType, rewardEvent.getFinalSkillPoints());
        dropMoney(player, dropLocation, rewardEvent.getFinalMoney());
        player.giveExp(rewardEvent.getFinalExp(), true);
        return true;
    }

    protected boolean giveStackedReward(Player player, ItemStack item, List<Block> blocks, MiningReward reward, Location dropLocation, int stackCount) {
        Session session = sessionOf(player);
        if (!session.isEnabled()) return false;
        final var rewardEvent = new SkillsBlockBreakRewardEvent(player, blocks,
                                                                reward.sp * stackCount,
                                                                session.computeMoneyDrop(skillType, reward.money * (double) stackCount),
                                                                (reward.exp + reward.veinExp + session.getExpBonus(skillType)) * stackCount);
        rewardEvent.callEvent();
        if (rewardEvent.isCancelled()) return false;
        skillsPlugin().getLogger().info("[Mining] [" + stackCount + "x" + blocks.get(0).getType() + "] " + player.getName() + " " + rewardEvent.debugString());
        session.addSkillPoints(skillType, rewardEvent.getFinalSkillPoints());
        dropMoney(player, dropLocation, rewardEvent.getFinalMoney());
        player.giveExp(rewardEvent.getFinalExp(), true);
        return true;
    }
}
