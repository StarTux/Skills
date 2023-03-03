package com.cavetale.skills.skill.archery;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class CrossbowHailTalent extends Talent implements Listener {
    public CrossbowHailTalent() {
        super(TalentType.XBOW_HAIL);
    }

    @Override
    public String getDisplayName() {
        return "Hailstorm";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Crossbow bullets fired way up high will rain down.",
                       "You can shoot :arrow:arrows with your"
                       + " :crossbow:crossbow way up in the air"
                       + " and watch them rain down on your opponents."
                       + "\n\nThese arrows will not hurt yourself.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.ANVIL);
    }

    protected void onShootCrossbow(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> {
                if (!arrow.isValid() || arrow.isDead()) return;
                Location location = arrow.getLocation();
                Vector velocity = arrow.getVelocity();
                double y = velocity.getY();
                if (y < 0.0 || (y * y) < Math.abs(velocity.getX() * velocity.getZ())) return;
                // Arrows face backwards!
                location.setDirection(new Vector(0.0, 1.0, 0.0));
                arrow.teleport(location);
                arrow.setVelocity(new Vector(velocity.getX(), -y, velocity.getZ()));
                ArrowType.HAIL.set(arrow);
            }, 10L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    /**
     * Avoid Hail self hit.
     */
    private void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (event.getHitEntity() == null) return;
        if (!Objects.equals(arrow.getShooter(), event.getHitEntity())) return;
        if (!(ArrowType.HAIL.is(arrow))) return;
        event.setCancelled(true);
    }
}
