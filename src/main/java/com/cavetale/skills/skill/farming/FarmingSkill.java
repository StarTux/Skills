package com.cavetale.skills.skill.farming;

import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.Util;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import com.cavetale.worldmarker.block.BlockMarker;
import com.destroystokyo.paper.MaterialTags;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Object to manage the Farming skill.
 * Called by EventListener et al, owned by SkillsPlugin.
 */
public final class FarmingSkill extends Skill implements Listener {
    public static final String WATERED_CROP = "skills:watered_crop";
    public static final String GROWN_CROP = "skills:grown_crop";
    public final GrowstickRadiusTalent growstickRadiusTalent;
    public final PlantRadiusTalent plantRadiusTalent;
    public final CropDropsTalent cropDropsTalent;
    public final DiamondDropsTalent diamondDropsTalent;
    public final TalentPointsTalent talentPointsTalent;

    public FarmingSkill(final SkillsPlugin plugin) {
        super(plugin, SkillType.FARMING);
        this.growstickRadiusTalent = new GrowstickRadiusTalent(plugin, this);
        this.plantRadiusTalent = new PlantRadiusTalent(plugin, this);
        this.cropDropsTalent = new CropDropsTalent(plugin, this);
        this.diamondDropsTalent = new DiamondDropsTalent(plugin, this);
        this.talentPointsTalent = new TalentPointsTalent(plugin, this);
    }

    @Override
    protected void enable() { }

    /**
     * Play the hoe effect.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    protected void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.FARMLAND) return;
        ItemStack item = event.getItemInHand();
        if (item == null || !MaterialTags.HOES.isTagged(item.getType())) return;
        Effects.hoe(block, event.getBlockReplacedState().getBlockData());
    }

    /**
     * Player uses a growstick on a certain block.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem()) return;
        final ItemStack item = Util.getHand(player, event.getHand());
        if (item.getType() != Material.STICK) return;
        final Block block = event.getClickedBlock();
        if (Crop.of(block) == null && block.getType() != Material.FARMLAND) return;
        int radius = 0;
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return;
        if (session.isTalentEnabled(TalentType.FARM_GROWSTICK_RADIUS)) radius = 1;
        boolean success = false;
        for (int dz = -radius; dz <= radius; dz += 1) {
            for (int dx = -radius; dx <= radius; dx += 1) {
                success |= waterBlock(player, block.getRelative(dx, 0, dz));
            }
        }
        if (success) Effects.wateringCan(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        Block block = event.getBlock();
        String bid = BlockMarker.getId(block);
        if (bid != null) {
            switch (bid) {
            case FarmingSkill.WATERED_CROP:
            case FarmingSkill.GROWN_CROP:
                BlockMarker.resetId(block);
                harvest(player, block);
                break;
            default: break;
            }
        }
    }

    /**
     * Crop harvesting.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onPlayerBreakBlock(PlayerBreakBlockEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        Block block = event.getBlock();
        String bid = BlockMarker.getId(block);
        if (bid != null) {
            switch (bid) {
            case FarmingSkill.WATERED_CROP:
            case FarmingSkill.GROWN_CROP:
                BlockMarker.resetId(block);
                harvest(player, block);
                break;
            default: break;
            }
        }
    }

    /**
     * Crop harvesting.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockFromTo(BlockFromToEvent event) {
        Block block = event.getBlock();
        String bid = BlockMarker.getId(block);
        if (bid != null) {
            switch (bid) {
            case FarmingSkill.WATERED_CROP:
            case FarmingSkill.GROWN_CROP:
                BlockMarker.resetId(block);
            default: break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    protected void onBlockGrow(BlockGrowEvent event) {
        if (BlockMarker.hasId(event.getBlock(), FarmingSkill.WATERED_CROP)) {
            event.setCancelled(true);
        }
    }

    protected boolean waterBlock(@NonNull Player player, @NonNull Block block) {
        if (block.getType() == Material.FARMLAND) {
            Block upper = block.getRelative(0, 1, 0);
            if (waterSoil(block) || waterCrop(player, upper)) {
                Effects.waterBlock(upper);
                return true;
            }
        } else {
            Block lower = block.getRelative(0, -1, 0);
            if (waterSoil(lower) || waterCrop(player, block)) {
                Effects.waterBlock(block);
                return true;
            }
        }
        return false;
    }

    /**
     * Attempt to water the block. Do nothing if it's not a crop, is
     * ripe, already watered, or has another block id.
     *
     * Play the effect and set the id otherwise.
     */
    protected boolean waterCrop(@NonNull Player player, @NonNull Block block) {
        if (Crop.of(block) == null) return false;
        if (isRipe(block)) return false;
        if (BlockMarker.hasId(block)) return false;
        BlockMarker.setId(block, WATERED_CROP);
        return true;
    }

    protected boolean waterSoil(@NonNull Block block) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Farmland)) return false;
        Farmland farmland = (Farmland) blockData;
        int max = farmland.getMaximumMoisture();
        if (farmland.getMoisture() >= max) return false;
        farmland.setMoisture(max);
        block.setBlockData(farmland);
        return true;
    }

    public static boolean isRipe(@NonNull Block block) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Ageable)) return false;
        Ageable ageable = (Ageable) blockData;
        return ageable.getAge() >= ageable.getMaximumAge();
    }

    protected void harvest(Player player, Block block) {
        Crop crop = Crop.of(block);
        if (crop == null) return;
        if (!isRipe(block)) return;
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return;
        // Extra crops
        if (session.isTalentEnabled(TalentType.FARM_CROP_DROPS)) {
            block.getWorld().dropItem(loc, new ItemStack(crop.itemMaterial,
                                                         plugin.random.nextInt(3) + 1));
        }
        // Special Rule
        if (crop == Crop.NETHER_WART || crop == Crop.BEETROOT) {
            if (plugin.random.nextBoolean()) return;
        }
        // Reward Diamond
        double gemChance = 0.01;
        final double roll = plugin.random.nextDouble();
        if (session.isTalentEnabled(TalentType.FARM_DIAMOND_DROPS)) gemChance = 0.02;
        if (roll < gemChance) {
            block.getWorld().dropItem(loc, new ItemStack(Material.DIAMOND));
            int inc = 1;
            if (session.isTalentEnabled(TalentType.FARM_TALENT_POINTS)) inc = 2;
            boolean noEffect = session.rollTalentPoint(inc);
            if (!noEffect) Effects.rewardJingle(loc);
        }
        // Exp
        session.addSkillPoints(SkillType.FARMING, 1);
        Util.exp(loc, 1 + session.getExpBonus(SkillType.FARMING));
        Effects.harvest(block);
    }
}
