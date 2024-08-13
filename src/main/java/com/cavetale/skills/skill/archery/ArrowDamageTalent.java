package com.cavetale.skills.skill.archery;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class ArrowDamageTalent extends Talent {
    public ArrowDamageTalent() {
        super(TalentType.ARROW_DAMAGE);
    }

    @Override
    public String getDisplayName() {
        return "Sniper";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Arrows from a fully charged bow receive bonus damage"
                       + " for every block they travel.",
                       "When your arrow hits its target, it will have picked up"
                       + " one additional damage for every 40 blocks distance from you.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.SPYGLASS);
    }

    protected void onArrowCollide(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        if (!arrow.isCritical() || arrow.isShotFromCrossbow()) return;
        if (!ArrowType.PRIMARY.is(arrow) && !ArrowType.BONUS.is(arrow)) return;
        final Location a = player.getLocation();
        final Location b = arrow.getLocation();
        if (!a.getWorld().equals(b.getWorld())) return;
        final double distance = a.distance(b);
        if (Double.isNaN(distance) || Double.isInfinite(distance) || distance <= 0.0) return;
        final double bonus = distance * 0.025;
        arrow.setDamage(arrow.getDamage() + bonus);
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType
                               + " d=" + String.format("%.2f", distance)
                               + " +" + String.format("%.2f", bonus));
        }
    }
};
