package com.cavetale.skills.skill.archery;

import com.cavetale.core.event.block.PlayerBlockAbilityQuery;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public final class CrossbowLingerTalent extends Talent {
    public CrossbowLingerTalent() {
        super(TalentType.XBOW_LINGER);
    }

    @Override
    public String getDisplayName() {
        return "Water Bomb";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Tipped crossbow bullets create a lingering effect cloud when they hit a block",
                       "When one of your crossbow :spectral_arrow:spectral or :tipped_arrow:tipped arrows"
                       + " hits the ground,"
                       + " it leaves behind a brief :ucloud:lingering effect cloud on the floor.");
    }

    @Override
    public ItemStack createIcon() {
        ItemStack icon = createIcon(Material.LINGERING_POTION);
        icon.editMeta(meta -> {
                if (meta instanceof PotionMeta potionMeta) {
                    potionMeta.setColor(Color.GREEN);
                }
            });
        return icon;
    }

    protected void onArrowHitBlock(Player player, AbstractArrow arrow, ProjectileHitEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (!arrow.isShotFromCrossbow()) return;
        Block block = event.getHitBlockFace() != null
            ? event.getHitBlock().getRelative(event.getHitBlockFace())
            : event.getHitBlock();
        if (!PlayerBlockAbilityQuery.Action.PLACE_ENTITY.query(player, block)) return;
        final Location location = block.getLocation().add(0.5, 0.125, 0.5);
        if (!location.getNearbyEntitiesByType(AreaEffectCloud.class, 0.5).isEmpty()) return;
        if (arrow instanceof SpectralArrow) {
            location.getWorld().spawn(location, AreaEffectCloud.class, aoe -> {
                    aoe.addCustomEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, false, true, true), false);
                    aoe.setColor(Color.WHITE);
                    prepareCloud(player, aoe);
                });
        } else if (arrow instanceof Arrow arrow2) {
            PotionData potionData = arrow2.getBasePotionData();
            if (potionData.getType() == PotionType.UNCRAFTABLE) return;
            location.getWorld().spawn(location, AreaEffectCloud.class, aoe -> {
                    aoe.setBasePotionData(potionData);
                    prepareCloud(player, aoe);
                });
        } else {
            return;
        }
        arrow.remove();
    }

    private static void prepareCloud(Player player, AreaEffectCloud aoe) {
        // # Default Values
        // Duration = 600
        // DurationOnUse = 0
        // Radius = 3.0
        // RadiusPerTick = 0
        // RadiusOnUse = 0
        // WaitTime = 20
        // ReapplicationDealy = 20
        // Velocity = (0, 0, 0)
        aoe.setPersistent(false);
        aoe.setSource(player);
        aoe.setDuration(100);
        aoe.setDurationOnUse(0);
        aoe.setRadius(1.5f);
        aoe.setRadiusPerTick(0.0f);
        aoe.setRadiusOnUse(0.0f);
        aoe.setWaitTime(0);
        aoe.setReapplicationDelay(20);
    }
}
