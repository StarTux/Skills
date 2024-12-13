package com.cavetale.skills.talent.archery;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.archery.ArrowType;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import static com.cavetale.skills.SkillsPlugin.random;
import static com.cavetale.skills.util.Text.formatDouble;

public final class VolleyTalent extends Talent {
    public VolleyTalent() {
        super(TalentType.VOLLEY, "Volley",
              "Multishot releases a volley of arrows",
              "Instead of 3 arrows flat, Multishot fires a barrage of :arrow:arrows.");
        addLevel(1, levelToBonusArrowCount(1) + " bonus arrow");
        addLevel(1, levelToBonusArrowCount(2) + " bonus arrows");
        addLevel(1, levelToBonusArrowCount(3) + " bonus arrows");
        addLevel(1, levelToBonusArrowCount(4) + " bonus arrows");
        addLevel(1, levelToBonusArrowCount(5) + " bonus arrows");
    }

    private static int levelToBonusArrowCount(int level) {
        return level;
    }

    @Override
    public ItemStack createIcon() {
        ItemStack icon = new ItemStack(Material.CROSSBOW);
        icon.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.values());
                ((CrossbowMeta) meta).setChargedProjectiles(List.of(new ItemStack(Material.FIREWORK_ROCKET)));
            });
        return icon;
    }

    public void onShootCrossbow(Player player, ItemStack crossbow, AbstractArrow arrow, ItemStack arrowItem) {
        if (!isPlayerEnabled(player)) {
            return;
        }
        if (arrowItem == null || !Tag.ITEMS_ARROWS.isTagged(arrowItem.getType())) {
            return;
        }
        final int multishot = crossbow.getEnchantmentLevel(Enchantment.MULTISHOT);
        if (multishot == 0) {
            return;
        }
        final int level = Session.of(player).getTalentLevel(talentType);
        if (level < 1) return;
        final int bonusArrowCount = levelToBonusArrowCount(level);
        final double velocity = arrow.getVelocity().length();
        final double damage = 1.0;
        int count = 0;
        for (int i = 0; i < bonusArrowCount; i += 1) {
            final Location location = player.getEyeLocation();
            location.setDirection(arrow.getVelocity());
            final float yaw = location.getYaw() + (float) ((random().nextDouble() * (random().nextBoolean() ? 1.0 : -1.0)) * 15.0);
            final float pitch = location.getPitch() + (float) ((random().nextDouble() * (random().nextBoolean() ? 1.0 : -1.0)) * 15.0);
            location.setYaw(yaw);
            location.setPitch(pitch);
            final AbstractArrow spam = player.launchProjectile(Arrow.class, location.getDirection().multiply(velocity), e -> {
                    e.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                    e.setWeapon(crossbow);
                    e.setCritical(true);
                    e.setDamage(damage);
                    ArrowType.SPAM.set(e);
                    ArrowType.NO_PICKUP.set(e);
                });
            if (spam == null) break;
            count += 1;
        }
        if (isDebugTalent(player)) {
            player.sendMessage(talentType
                               + " multi:"
                               + multishot
                               + " arrows:" + count + "/" + bonusArrowCount
                               + " velo:" + formatDouble(velocity)
                               + " damage:" + formatDouble(damage));
        }
    }
}
