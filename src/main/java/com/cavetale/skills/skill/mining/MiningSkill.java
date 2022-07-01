package com.cavetale.skills.skill.mining;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.util.Players;
import com.destroystokyo.paper.MaterialTags;
import com.winthier.exploits.Exploits;
import java.util.EnumMap;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public final class MiningSkill extends Skill implements Listener {
    protected final EnumMap<Material, MiningReward> rewards = new EnumMap<>(Material.class);
    public final StripMiningTalent stripMiningTalent;
    public final VeinMiningTalent veinMiningTalent;
    public final VeinGemsTalent veinGemsTalent;
    public final VeinMetalsTalent veinMetalsTalent;
    public final SilkStripTalent silkStripTalent;
    public final SilkMetalsTalent silkMetalsTalent;
    public final SilkMultiTalent silkMultiTalent;
    public final MinerSightTalent minerSightTalent;
    public final SuperVisionTalent superVisionTalent;
    public final NetherVisionTalent netherVisionTalent;
    public final OreAlertTalent oreAlertTalent;
    public final EmeraldAlertTalent emeraldAlertTalent;
    public final DebrisAlertTalent debrisAlertTalent;

    public MiningSkill(@NonNull final SkillsPlugin plugin) {
        super(plugin, SkillType.MINING);
        this.stripMiningTalent = new StripMiningTalent(plugin, this);
        this.veinMiningTalent = new VeinMiningTalent(plugin, this);
        this.veinGemsTalent = new VeinGemsTalent(plugin, this);
        this.veinMetalsTalent = new VeinMetalsTalent(plugin, this);
        this.silkStripTalent = new SilkStripTalent(plugin, this);
        this.silkMetalsTalent = new SilkMetalsTalent(plugin, this);
        this.silkMultiTalent = new SilkMultiTalent(plugin, this);
        this.minerSightTalent = new MinerSightTalent(plugin, this);
        this.superVisionTalent = new SuperVisionTalent(plugin, this);
        this.netherVisionTalent = new NetherVisionTalent(plugin, this);
        this.oreAlertTalent = new OreAlertTalent(plugin, this);
        this.emeraldAlertTalent = new EmeraldAlertTalent(plugin, this);
        this.debrisAlertTalent = new DebrisAlertTalent(plugin, this);
    }

    @Override
    protected void enable() {
        // exp values are maxima according to the wiki
        // Last three values are used by Silk Fortune, "null, 0, null" any values to disable
        reward(Material.DIAMOND_ORE, 10, 7, Material.DIAMOND, 1, Material.STONE);
        reward(Material.DEEPSLATE_DIAMOND_ORE, 10, 7, Material.DIAMOND, 1, Material.DEEPSLATE);
        reward(Material.EMERALD_ORE, 10, 7, Material.EMERALD, 1, Material.STONE);
        reward(Material.DEEPSLATE_EMERALD_ORE, 10, 7, Material.EMERALD, 1, Material.DEEPSLATE);
        reward(Material.IRON_ORE, 3, 3, Material.RAW_IRON, 1, Material.STONE);
        reward(Material.DEEPSLATE_IRON_ORE, 3, 3, Material.RAW_IRON, 1, Material.DEEPSLATE);
        reward(Material.COPPER_ORE, 1, 3, Material.RAW_COPPER, 1, Material.STONE);
        reward(Material.DEEPSLATE_COPPER_ORE, 1, 3, Material.RAW_COPPER, 1, Material.DEEPSLATE);
        reward(Material.GOLD_ORE, 5, 3, Material.RAW_GOLD, 1, Material.STONE);
        reward(Material.DEEPSLATE_GOLD_ORE, 5, 3, Material.RAW_GOLD, 1, Material.DEEPSLATE);
        reward(Material.NETHER_GOLD_ORE, 5, 3, Material.GOLD_NUGGET, 4, Material.NETHERRACK); // 2-6
        reward(Material.GILDED_BLACKSTONE, 5, 3, Material.GOLD_NUGGET, 0, Material.BLACKSTONE); // 10% to drop 2-5
        reward(Material.COAL_ORE, 1, 2, Material.COAL, 1, Material.STONE);
        reward(Material.DEEPSLATE_COAL_ORE, 1, 2, Material.COAL, 1, Material.DEEPSLATE);
        reward(Material.LAPIS_ORE, 1, 5, Material.LAPIS_LAZULI, 7, Material.STONE); // 4-9
        reward(Material.DEEPSLATE_LAPIS_ORE, 1, 5, Material.LAPIS_LAZULI, 7, Material.DEEPSLATE);
        reward(Material.NETHER_QUARTZ_ORE, 1, 5, Material.QUARTZ, 1, Material.NETHERRACK);
        reward(Material.REDSTONE_ORE, 1, 5, Material.REDSTONE, 5, Material.STONE); // 4-5
        reward(Material.DEEPSLATE_REDSTONE_ORE, 1, 5, Material.REDSTONE, 5, Material.DEEPSLATE);
        reward(Material.ANCIENT_DEBRIS, 20, 10, Material.NETHERITE_SCRAP, 1, Material.NETHERRACK);
        // not ores
        reward(Material.RAW_COPPER_BLOCK, 5, 3, null, 0, null);
        reward(Material.RAW_IRON_BLOCK, 15, 3, null, 0, null);
        reward(Material.RAW_GOLD_BLOCK, 25, 3, null, 0, null); // currently does not generate
        reward(Material.BUDDING_AMETHYST, 10, 1, Material.AMETHYST_SHARD, 3, Material.AMETHYST_BLOCK);
    }

    private void reward(@NonNull Material material, final int sp, final int exp, Material item, int drops, Material replaceable) {
        rewards.put(material, new MiningReward(material, sp, exp, item, drops, replaceable));
    }

    protected static boolean stone(@NonNull Block block) {
        switch (block.getType()) {
        case STONE:
        case DEEPSLATE:
        case TUFF:
        case DIORITE:
        case ANDESITE:
        case GRANITE:
            return true;
        default:
            return false;
        }
    }

    protected static boolean dirt(@NonNull Block block) {
        switch (block.getType()) {
        case GRAVEL:
        case DIRT:
            return true;
        default:
            return false;
        }
    }
// unused
/*
    protected static boolean basicOre(@NonNull Block block) {
        switch (block.getType()) {
        case COAL_ORE:
        case DEEPSLATE_COAL_ORE:
        case REDSTONE_ORE:
        case DEEPSLATE_REDSTONE_ORE:
        case LAPIS_ORE:
        case DEEPSLATE_LAPIS_ORE:
            return true;
        default:
            return false;
        }
    }
*/
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
        giveReward(player, block, reward);
    }

    /**
     * Give the SP reward for the broken block and roll talent points
     * where it applies.
     *
     * Do NOT give exp rewards as their spawning location is
     * situational.
     *
     * Do NOT drop any items because they only drop when silk
     * stripping.
     */
    protected boolean giveReward(Player player, Block block, MiningReward reward) {
        if (Exploits.isPlayerPlaced(block)) return false;
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return false;
        session.addSkillPoints(SkillType.MINING, reward.sp);
        Material mat = block.getType();
        return true;
    }
}
