package com.cavetale.skills.skill.archery;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class ArrowDamageTalent extends Talent implements Listener {
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
                       + " one additional damage for every 20 blocks distance from you.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.SPYGLASS);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onProjectileCollide(ProjectileCollideEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow) || arrow instanceof Trident) return;
        if (!(arrow.getShooter() instanceof Player player)) return;
        if (!isPlayerEnabled(player)) return;
        if (!arrow.isCritical()) return;
        Location a = player.getLocation();
        Location b = arrow.getLocation();
        if (!a.getWorld().equals(b.getWorld())) return;
        double distance = a.distance(b);
        if (Double.isNaN(distance) || Double.isInfinite(distance) || distance <= 0.0) return;
        double bonus = distance * 0.05;
        arrow.setDamage(arrow.getDamage() + bonus);
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType
                               + " d=" + String.format("%.2f", distance)
                               + " +" + String.format("%.2f", bonus));
        }
    }
};
