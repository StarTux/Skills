package com.cavetale.skills.skill.farming;

import com.cavetale.core.event.block.PlayerBlockAbilityQuery;
import com.cavetale.core.event.block.PlayerChangeBlockEvent;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import com.winthier.exploits.Exploits;
import java.util.ArrayList;
import java.util.Collections;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class PlantRadiusTalent extends Talent implements Listener {
    protected final FarmingSkill farmingSkill;

    protected PlantRadiusTalent(final SkillsPlugin plugin, final FarmingSkill farmingSkill) {
        super(plugin, TalentType.FARM_PLANT_RADIUS);
        this.farmingSkill = farmingSkill;
    }

    @Override
    protected void enable() { }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        if (player.isSneaking()) return;
        if (!event.hasItem()) return;
        final ItemStack item = event.getItem();
        Crop crop = Crop.ofSeed(item);
        if (crop == null) return;
        final Block block = event.getClickedBlock();
        Material soil = crop == Crop.NETHER_WART
            ? Material.SOUL_SAND
            : Material.FARMLAND;
        if (block.getType() == soil) {
            plantRadius(player, block.getRelative(0, 1, 0), crop, item);
            return;
        }
        Block lower = block.getRelative(0, -1, 0);
        if (lower.getType() == soil) {
            plantRadius(player, block, crop, item);
            return;
        }
    }

    protected int plantRadius(Player player, Block orig, Crop crop, ItemStack item) {
        int result = 0;
        ArrayList<Block> bs = new ArrayList<>(8);
        for (int z = -1; z <= 1; z += 1) {
            for (int x = -1; x <= 1; x += 1) {
                bs.add(orig.getRelative(x, 0, z));
            }
        }
        Collections.shuffle(bs, plugin.random);
        for (Block block : bs) {
            if (item.getType() != crop.seedMaterial) break;
            if (item.getAmount() < 1) break;
            if (!block.isEmpty()) continue;
            Block lower = block.getRelative(0, -1, 0);
            if (crop == Crop.NETHER_WART && lower.getType() != Material.SOUL_SAND) continue;
            if (crop != Crop.NETHER_WART && lower.getType() != Material.FARMLAND) continue;
            if (!PlayerBlockAbilityQuery.Action.BUILD.query(player, block)) continue;
            new PlayerChangeBlockEvent(player, block, crop.blockMaterial.createBlockData()).callEvent();
            block.setType(crop.blockMaterial);
            Exploits.setPlayerPlaced(block, true);
            item.setAmount(item.getAmount() - 1);
            Effects.plantCropMagic(block);
            result += 1;
        }
        return result;
    }
}
