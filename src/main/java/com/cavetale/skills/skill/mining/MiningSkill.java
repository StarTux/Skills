package com.cavetale.skills.skill.mining;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.Util;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.destroystokyo.paper.MaterialTags;
import com.winthier.exploits.Exploits;
import java.util.EnumMap;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.Tag;
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
    public final OreAlertTalent oreAlertTalent;
    public final XrayTalent xrayTalent;
    public final SilkStripTalent silkStripTalent;
    public final SilkFortuneTalent silkFortuneTalent;

    public MiningSkill(@NonNull final SkillsPlugin plugin) {
        super(plugin, SkillType.MINING);
        this.stripMiningTalent = new StripMiningTalent(plugin, this);
        this.oreAlertTalent = new OreAlertTalent(plugin, this);
        this.xrayTalent = new XrayTalent(plugin, this);
        this.silkStripTalent = new SilkStripTalent(plugin, this);
        this.silkFortuneTalent = new SilkFortuneTalent(plugin, this);
    }

    @Override
    protected void enable() {
        // exp values are maxima according to the wiki
        reward(Material.DIAMOND_ORE, 10, 7, Material.DIAMOND, 1, Material.STONE);
        reward(Material.DEEPSLATE_DIAMOND_ORE, 10, 7, Material.DIAMOND, 1, Material.DEEPSLATE);
        reward(Material.EMERALD_ORE, 10, 7, Material.EMERALD, 1, Material.STONE);
        reward(Material.DEEPSLATE_EMERALD_ORE, 10, 7, Material.EMERALD, 1, Material.DEEPSLATE);
        reward(Material.IRON_ORE, 3, 3, Material.IRON_NUGGET, 9, Material.STONE);
        reward(Material.DEEPSLATE_IRON_ORE, 3, 3, Material.IRON_NUGGET, 9, Material.DEEPSLATE);
        reward(Material.COPPER_ORE, 3, 3, Material.RAW_COPPER, 1, Material.STONE);
        reward(Material.DEEPSLATE_COPPER_ORE, 3, 3, Material.RAW_COPPER, 1, Material.DEEPSLATE);
        reward(Material.GOLD_ORE, 5, 3, Material.GOLD_NUGGET, 9, Material.STONE);
        reward(Material.DEEPSLATE_GOLD_ORE, 5, 3, Material.GOLD_NUGGET, 9, Material.DEEPSLATE);
        reward(Material.NETHER_GOLD_ORE, 5, 3, Material.GOLD_NUGGET, 9, Material.NETHERRACK);
        reward(Material.GILDED_BLACKSTONE, 5, 3, Material.GOLD_NUGGET, 0, Material.BLACKSTONE);
        reward(Material.COAL_ORE, 1, 2, Material.COAL, 1, Material.STONE);
        reward(Material.DEEPSLATE_COAL_ORE, 1, 2, Material.COAL, 1, Material.DEEPSLATE);
        reward(Material.LAPIS_ORE, 1, 5, Material.LAPIS_LAZULI, 6, Material.STONE); // 4-8
        reward(Material.DEEPSLATE_LAPIS_ORE, 1, 5, Material.LAPIS_LAZULI, 6, Material.DEEPSLATE);
        reward(Material.NETHER_QUARTZ_ORE, 1, 5, Material.QUARTZ, 1, Material.NETHERRACK);
        reward(Material.REDSTONE_ORE, 1, 5, Material.REDSTONE, 5, Material.STONE); // 4-5
        reward(Material.DEEPSLATE_REDSTONE_ORE, 1, 5, Material.REDSTONE, 5, Material.DEEPSLATE);
        reward(Material.ANCIENT_DEBRIS, 20, 10, Material.NETHERITE_SCRAP, 1, Material.NETHERRACK); // 4-5
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
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
        if (Tag.DIAMOND_ORES.isTagged(mat) || Tag.EMERALD_ORES.isTagged(mat)) {
            session.rollTalentPoint(1);
        }
        return true;
    }
}
